/**
* Copyright (c) 2015 EMC Corporation
* All Rights Reserved
*
* This software contains the intellectual property of EMC Corporation
* or is licensed to EMC Corporation from third parties.  Use of this
* software and the intellectual property contained therein is expressly
* limited to the terms and conditions of the License Agreement under which
* it is provided by or on behalf of EMC.
*
**/

downloadUrl = "";
selected = "";
var element = document.createElement("input");
element.type = "button";
element.value = "  Export  ";
element.name = "button";
element.id = "exportFile";
element.style.backgroundColor = '#808080';
element.style.color = '#E6E6E6'



function isBrowserIE() {
    var ua = window.navigator.userAgent;
    var msie = ua.indexOf('MSIE ');
    if (msie > 0) {
        // IE 10 or older => return version number
        return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
    }

    var trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11 => return version number
        var rv = ua.indexOf('rv:');
        return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
    }

    var edge = ua.indexOf('Edge/');
    if (edge > 0) {
       // IE 12 => return version number
       return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
    }

    // other browser
    return false;
}

var getPathnameFromUrl = function(href) {
    var link = document.createElement("a");
    link.href = href;
    var pathname = link.pathname
    delete link;
    return pathname;
};


var TranslateUrlOnBrowser = function(url) {

  var transformed_url = "";
  if ( !isBrowserIE() ) {
    // add /kibana
    var link = document.createElement("a");
    link.href = url;
    var protocol = link.protocol
    var host = link.host
    var pathname = link.pathname
    pathname = "/kibana" + pathname;

    if (protocol != undefined && protocol != ""){
      transformed_url = transformed_url + protocol + "//"
    }

    if (host != undefined && host != ""){
      transformed_url = transformed_url + host
    }

    if (pathname != undefined && pathname != ""){
      transformed_url = transformed_url + pathname
    }

    delete link;
  }else{
    transformed_url = url;
  }

  return transformed_url
}

var TranslateUrlOnProtocol = function( url ) {
  if (document.location.protocol == "https:" ) {
    var pathname = getPathnameFromUrl(url);
    return pathname;
  }else {
    return url;
  }
}


function export1(day_value) {
    var textFile = null;
    var date_range = "now-7d"
    if (day_value == '15') {
        date_range = "now-15d"
    } else if (day_value == '30') {
        date_range = "now-30d"
    }

    var query1 = {
        "query": {
            "filtered": {
                "filter": {
                    "range": {
                        "@timestamp": {
                            "gt": date_range
                        }
                    }
                }
            }
        }
    };


    function downloadFile() {

        $.fileDownload( TranslateUrlOnProtocol(TranslateUrlOnBrowser(downloadUrl)), {
             successCallback: function (url) {
                  console.log('You just got a file download dialog or ribbon for this URL :' + url);
             },
             failCallback: function (html, url) {
               setTimeout(downloadFile, 5000);
             }
         });
    }


    function makeRequest() {
        $.ajax({
            url: TranslateUrlOnProtocol(TranslateUrlOnBrowser(downloadUrl)),
            type: 'GET',
            statusCode: {
                200: function() {
                    closeModal();
                    setTimeout(downloadFile, 100);
                },
                503: function() {
                    setTimeout(makeRequest, 8000);
                }
            }
        });
    }

    var request = $.ajax({
        url: TranslateUrlOnProtocol(TranslateUrlOnBrowser('export_url/api/logs/archive')),
        type: 'POST',
        crossDomain: true,
        dataType: 'text',
        contentType: 'application/json',
        data: JSON.stringify(query1),

        success: function(response, status, xhr) {
            console.log('Hi, your download will be available in the following link in a few minutes - ' + response);
            popUpDownload(response)
            downloadUrl = response;
            makeRequest();
        },

        error: function(jqXHR, textStatus, errorThrown) {
            alert('Sorry your request could not be processed. Try again');
        }
    });

}

setInterval(function() {
    $('navbar').append(element);
  //  $('navbar').append(fileUrl);
    $(element).click(fadeInPopUp);
}, 500);




function fadeInPopUp(event) {
    event.preventDefault();
    event.stopPropagation()
        //check if modal exist and destroy
    var modal = document.getElementById('myModal');
    if (modal) {
        document.body.removeChild(modal);
    }

    //create modal container
    var modalContainer = $('<div>', {
        'id': 'myModal',
        'class': "jsPlugin modal fade in"
    });


    //create modal dialogue
    var modalDialog = $('<div>', {
        'class': "modal-dialog modal-lg"
    });
    modalDialog.css('width', '350px');
    $('#myModal').showCloseButton;
    //create modal content
    var modalContent = $('<div>', {
            'class': "modal-content"
        })
        //create modal body
    var modalBody = $('<div>', {
        'class': "modal-body formViewer"
    });

    //create close button for modal close
    var closeButton = $('<a>', {
        'class': "jsPlugin-btn jsPlugin-btn-xs jsPlugin-btn-danger text-danger",
        'id': "closeButton",
        'style': "display:none;",
    });

    closeButton.click(closeModal);
    closeButton.innerHTML = '&nbsp;x&nbsp';
    var popUpList = '<div> <b> How many days logs would you like to export? </b><br><br><input type="radio" class="cradio" name="day" checked="true" value="7"> 7<br><input type="radio" class="cradio" name="day" value="15"> 15<br><input type="radio" class="cradio" name="day" value="30"> 30<br> <button type = "button" id = "cancelButton" style="color:black;margin-left:50px;width:70px;background-color:#e3e3e3"><b> Cancel </b></button> <button type = "button" id = "okayButton" style="color:Black;margin-left:70px;width:70px;background-color:#e3e3e3" ><b> Okay </b></button></div>';
    var div = document.createElement("div")
    div.innerHTML = popUpList
    //appent modal body elements
    modalBody.append(closeButton);
    modalBody.append(popUpList);
    //append modal body to dialogue
    modalContent.append(modalBody);
    //appent modal content to dialogue
    modalDialog.append(modalContent);
    //appent modal dialogue to container
    modalContainer.append(modalDialog);
    //show modal
    modalContainer.css({
        'display': 'block'
    });

    if (document.body != null) {
        modalContainer.appendTo('body');
        $('#okayButton').click(closeModalOkay);
        $('#cancelButton').click(closeModal);
        $(".cradio").change(function() {
            $('.cradio').not(this).prop('checked', false);
        });
    }
}

function closeModal() {
    var modal = $('#myModal');
    var id = 'myElementId';
    modal.css("display", "none");
}


function closeModalOkay(){
        var modal = $('#myModal');
        var id='myElementId';
        var days = document.getElementsByName('day');
        var day_value;
        for(var i = 0; i < days.length; i++){
        if(days[i].checked){
                day_value = days[i].value;
            }
        }
        console.log(day_value)
        export1(day_value);
          modal.css("display" ,"none");
}

function popUpDownload(response) {
    // event.preventDefault();
    // event.stopPropagation()
        //check if modal exist and destroy
    var modal = document.getElementById('myModal');
    if (modal) {
        document.body.removeChild(modal);
    }

    //create modal container
    var modalContainer = $('<div>', {
        'id': 'myModal',
        'class': "jsPlugin modal fade in"
    });


    //create modal dialogue
    var modalDialog = $('<div>', {
        'class': "modal-dialog modal-lg"
    });
    modalDialog.css('width', '350px');
    //create modal content
    var modalContent = $('<div>', {
            'class': "modal-content"
        })
        //create modal body
    var modalBody = $('<div>', {
        'class': "modal-body formViewer"
    });
    var div = document.createElement("div1")

    var popUpContent = '<div> We are processing your request, your download will be available shortly at <a href="'+response + '" target="_parent" style="color:blue">' +response + '</a> <br> <br> You can retain the download-link for future download and deletion of archive from server <br> <br> To delete the archive from server: <i>curl -XDELETE ' + response + ' </i><br><br><button type = "button" id = "okayButtonDownload" style="color:Black;margin-left:120px;width:70px;background-color:#e3e3e3" ><b> Okay </b></button></div> '

    div.innerHTML = popUpContent
    //appent modal body elements
    modalBody.append(popUpContent);
    //append modal body to dialogue
    modalContent.append(modalBody);
    //appent modal content to dialogue
    modalDialog.append(modalContent);
    //appent modal dialogue to container
    modalContainer.append(modalDialog);
    //show modal
    modalContainer.css({
        'display': 'block'
    });

    if (document.body != null) {
        modalContainer.appendTo('body');
         $('#okayButtonDownload').click(closeModal);
    }
}
var css_link = $("<link>", {
    rel: "stylesheet",
    type: "text/css",
    href: "/elasticdump.css"
});
css_link.appendTo('head');
