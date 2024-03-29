//EMC Corporation, (c) 2015
// Changes made in app.js file to add a middleware for keystone
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var requestLogger = require('./lib/requestLogger');
var auth = require('./lib/auth');
var appHeaders = require('./lib/appHeaders');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var compression = require('compression');
var config = require('./config');
var xsrf = require('./lib/xsrf');
var request = require('request');
var routes = require('./routes/index');
var proxy = require('./routes/proxy');
var app = express();
var parser = require('./parser.js');

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.set('x-powered-by', false);
function authMiddleware (req, res, next) {
   //check auth token here, if not authorized/authenticated

    var subjectToken;
    function getAdminToken(){
        var adminToken;
        request.post({
            headers: {'content-type' : 'application/json'},
            url:     'keystone_url/v3/auth/tokens',
            rejectUnhauthorized : false,
            strictSSL: false,
            json:    {
                "auth":
                {
                    "identity":
                    {
                        "methods": [
                        "password"
                        ],
                        "password": {
                            "user": {
                            "domain" : {
                                "id":"default"
                                },
                                "name": "csa",
                                "password": process.env.CSA_PWD
                            }
                        }
                    },
                    "scope": {
                        "domain":{
                            "id":"default"
                        }
                    }
                }
            }
        },
        function(error, response, body){
            adminToken = response.headers['x-subject-token'];
            checkValidToken(adminToken);
        });
    }
    function checkContainsHeader() {
    //Check if request has an auth token

    var containsHeader = 0;
    if (req.headers['cookie']!=undefined) {
          var cookie = (req.headers['cookie']);
          if(cookie.indexOf('X-Auth-Token')!=-1) {
            containsHeader=1;
            subjectToken = parser(cookie);
     }
    }

    //Redirect if it does not have an auth token
    if(!containsHeader){
        return res.send('Your session has timed out.Please login again');
    }

    else {
      getAdminToken();
    }
  }
    //Else check if user is authorized
  function checkValidToken(adminToken) {
        //Admin Token is not loaded in time, given an error
 request.get({
            headers: {
                'content-type' : 'application/json',
                'X-Subject-Token' : subjectToken,
                'X-Auth-Token' : adminToken
            },
            url:  'keystone_url/v3/auth/tokens',
            rejectUnhauthorized : false,
            strictSSL: false,
            },
            function(error, response, body){
                //Check authorization , admin for now
                if (response.statusCode == 200) {
                  checkAuthorized(body);

                }
                else {
                    return res.send('Your session has timed out.Please login again');

                }
          });
      }

      function checkAuthorized(responseBody) {
        var authorized = 0;
        body = JSON.parse(responseBody);
        roles = body.token.roles;
                    for ( i in roles){
                        if(roles[i].name == 'admin' || roles[i].name == 'monitor'){
                        authorized=1;
                        }
      

                    }
        if(!authorized){
                 return res.send('Your session has timed out.Please login again');

        }
        next()
      }
  checkContainsHeader();
}
app.use(authMiddleware)

app.use(requestLogger());
app.use(auth());
app.use(xsrf(config.kibana.xsrf_token));
app.use(appHeaders());
app.use(favicon(path.join(config.public_folder, 'styles', 'theme', 'elk.ico')));

if (app.get('env') === 'development') {
  require('./dev')(app);
}

// The proxy must be set up before all the other middleware.
// TODO: WE might want to move the middleware to each of the individual routes
// so we don't have weird conflicts in the future.
app.use('/elasticsearch', proxy);

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(compression());
app.use(express.static(config.public_folder));
if (config.external_plugins_folder) app.use('/plugins', express.static(config.external_plugins_folder));
app.use('/', routes);


// catch 404 and forward to error handler
app.use(function (req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function (err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function (err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});


module.exports = app;
