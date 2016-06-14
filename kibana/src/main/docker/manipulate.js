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
///////////////////////////////////////
var dash_flag=0, firstLoad = true;
var discover_first=0;
function monthName(i){
	switch(i){
		case '01':
			return 'January';
		case '02':
			return 'February';
		case '03':
			return 'March';
		case '04':
			return 'April';
		case '05':
			return 'May';
		case '06':
			return 'June';
		case '07':
			return 'July';
		case '08':
			return 'August';
		case '09':
			return 'September';
		case '10':
			return 'October';
		case '11':
			return 'November';
		case '12':
			return 'December';
	}
}

function timestamp(message){
	var i = message.indexOf('@timestamp:');
	i = message.indexOf('@timestamp',i+1);
	i = message.indexOf(':',i);
	i = i+2;
	var j = message.indexOf('"',i);
	var result = message.substring(i,j);
	var year = result.substring(0,4),
		month = result.substring(5,7),
		day = result.substring(8,10),
		time = result.substring(11,23);
	result = monthName(month) + ' ' + day + ' ' + year + ', ' + time;
	return result;
}
function manipulateTimeStampFiltered(){
	var i =0,j=0;
	$('.kbn-table th').each(function(){
		console.log($(this).text())
		if($(this).text().indexOf('@timestamp') > -1)
			j=i;
		i++;
	})
	$($('.kbn-table tbody')[0]).find('tr').each(function(){
		$($(this).find('td')[j]).text($($(this).find('td')[1]).text());
	})
}
function manipulateTimeStamp(callback){
	$('.discover-table-row').each(function(){
		$($(this).find('td')[1]).text(timestamp($($(this).find('td')[2]).html()));
	});
	callback();
}
function defaultDashboard(){
	$($('.nav li')[3]).click(function () {
		setTimeout(function(){
			window.location='/kibana/#/dashboard/DEFAULT-DASHBOARD';
		},100);
	});
	console.log('default DashBoard script updated')
}

function defaultColumns(){
        $($('.nav li')[1]).click(function () {
                console.log('discover clicked');
                if(discover_first==0) {
                var currUrl = window.location.toString();
                var gUrlPart = currUrl.substring(currUrl.indexOf('?')+4,currUrl.indexOf('&'));
                console.log('adding default columns');
                setTimeout(function(){
                        window.location='/kibana/#/discover?_g='+gUrlPart+'&_a=(columns:!(severity,category,devtype,device,parttype,part,message))';
                },100);
                console.log('default Columns added');
                discover_first=1
                console.log( window.location.href);
                checkingUrl();
             }
        });
}


function checkingUrl() {
console.log('in function checking url');
  $($('.nav li')[1]).click(function () {
        console.log('discover');
        if(discover_first>0) {
        console.log('checking url');
        var currentUrl = window.location.href;
        var currentUrlString = currentUrl.toString();
        var url = window.location.hash;
        var urlString = url.toString();
        var defaultColumnsUrl = "#/discover?_g=()&_a=(columns:!(severity,category,devtype,device,parttype,part,message),index:'logstash-*',interval:auto,query:'',sort:!('@timestamp',desc))"
        var defaultColumnsUrl2 = "#/discover?_a=(columns:!(severity,category,devtype,device,parttype,part,message),index:'logstash-*',interval:auto,query:'',sort:!('@timestamp',desc))&_g=()"
        if(urlString == defaultColumnsUrl || urlString == defaultColumnsUrl2)
        {
                console.log('url matched ');
                setTimeout(function(){
                window.location = currentUrlString;
                checkingUrl();
                },100);
        }
                checkingUrl();
        }
        });
}


function manipulateColor(){
	d3.selectAll('.slice')
	.each(function(d){
		if((d['name']).toLowerCase() == 'error'){
			d3.select(this).style('fill','#F15A5A');
		}
		if((d['name']).toLowerCase() == 'critical' ){
			d3.select(this).style('fill','#B70000');
		}
		if((d['name']).toLowerCase() == 'severe' ){
			d3.select(this).style('fill','#5C1111');
		}
		if((d['name']).toLowerCase() == 'major'){
			d3.select(this).style('fill','#FD712C');
		}
		if((d['name']).toLowerCase() == 'fine'){
			d3.select(this).style('fill','#6FBCD8');
		}
		if((d['name']).toLowerCase() == 'finer'){
			d3.select(this).style('fill','#6F87D8');
		}
		if((d['name']).toLowerCase() == 'finest'){
			d3.select(this).style('fill','#006E8A');
		}
		if((d['name']).toLowerCase() == 'trace'){
			d3.select(this).style('fill','#DAA05D');
		}
		if((d['name']).toLowerCase() == 'note'){
			d3.select(this).style('fill','#D86F87');
		}
		if((d['name']).toLowerCase() == 'minor'){
			d3.select(this).style('fill','#FCFC2D');
		}
		if((d['name']).toLowerCase() == 'warning'){
			d3.select(this).style('fill','yellow');
		}
		if((d['name']).toLowerCase() == 'unknown'){
			d3.select(this).style('fill','#8B4EC4');
		}
		if((d['name']).toLowerCase() == 'info'){
			d3.select(this).style('fill','#57C17B');
		}
		if((d['name']).toLowerCase() == 'debug'){
			d3.select(this).style('fill','#D1D1C3');
		}

	});

	d3.selectAll('.lines')
		.each(function(){
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'critical' || d3.select(this).data()[0]['label'].toLowerCase() == 'error'){
				d3.select(this).select('path')
				.attr('stroke','#F15A5A');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'major'){
				d3.select(this).select('path')
				.attr('stroke','#FD712C');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'minor' || d3.select(this).data()[0]['label'].toLowerCase() == 'warning'){
				d3.select(this).select('path')
				.attr('stroke','#FCFC2D');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'unknown'){
				d3.select(this).select('path')
				.attr('stroke','#6F87D8');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'info'){
				d3.select(this).select('path')
				.attr('stroke','#57C17B');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'debug'){
				d3.select(this).select('path')
				.attr('stroke','#D1D1C3');
			}

		});

      $('.vis-wrapper rect')
		.each(function(){
		if(d3.select(this).data()[0]['label']!=undefined) {
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'error'){
				d3.select(this).style('fill','#F15A5A');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'critical'){
				d3.select(this).style('fill','#B70000');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'severe'){
				d3.select(this).style('fill','#5C1111');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'note'){
				d3.select(this).style('fill','#D86F87');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'fine'){
				d3.select(this).style('fill','#6FBCD8');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'finest'){
				d3.select(this).style('fill','#006E8A');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'finer'){
				d3.select(this).style('fill','#6F87D8');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'trace'){
				d3.select(this).style('fill','#DAA05D');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'minor' || d3.select(this).data()[0]['label'].toLowerCase() == 'warning'){
				d3.select(this).style('fill','#FCFC2D');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'unknown'){
				d3.select(this).style('fill','#8B4EC4');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'info'){
				d3.select(this).style('fill','#57C17B');
			}
			if(d3.select(this).data()[0]['label'].toLowerCase() == 'debug'){
				d3.select(this).style('fill','#D1D1C3');
			} }

		});



	$('.legend-ul li').each(function(){
		if($(this).text().toLowerCase() == 'error'){
				$(this).find('i').css('color','#F15A5A');
		}
		if($(this).text().toLowerCase() == 'critical' ){
				$(this).find('i').css('color','#B70000');
		}
		if($(this).text().toLowerCase() == 'severe' ){
				$(this).find('i').css('color','#5C1111');
		}
		if($(this).text().toLowerCase() == 'note' ){
				$(this).find('i').css('color','#D86F87');
		}
		if($(this).text().toLowerCase() == 'major'){
				$(this).find('i').css('color','#FD712C');
		}
		if($(this).text().toLowerCase() == 'finest'){
				$(this).find('i').css('color','#006E8A');
		}
		if($(this).text().toLowerCase() == 'fine'){
				$(this).find('i').css('color','#6FBCD8');
		}
		if($(this).text().toLowerCase() == 'finer'){
				$(this).find('i').css('color','#6F87D8');
		}
		if($(this).text().toLowerCase() == 'trace'){
				$(this).find('i').css('color','#DAA05D');
		}
		if($(this).text().toLowerCase() == 'minor'){
				$(this).find('i').css('color','#FCFC2D');
		}
		if($(this).text().toLowerCase() == 'warning'){
				$(this).find('i').css('color','#FCFC2D');
		}
		if($(this).text().toLowerCase() == 'unknown'){
				$(this).find('i').css('color','#8B4EC4');
		}
		if($(this).text().toLowerCase() == 'info'){
				$(this).find('i').css('color','#57C17B');
		}
		if($(this).text().toLowerCase() == 'debug'){
				$(this).find('i').css('color','#D1D1C3');
		}
	});
}



var changeCondensedTimestamp = function(){
	$('.discover-table-row').each(function(){
		$($(this).find('td')[0]).click(function(){
			var t = ($(this).next('td').text());
			$($(this).parent('tr').next().find('.table-condensed tbody td')[2]).text(t)
		})
	})
}
function defaultIndex(){
	console.log('Loading default index script')
	$($('.nav li')[4]).click(function () {
		firstLoad = false;
	})
	setInterval(function(){
		if ( $('.kbn-settings-indices-create option').length > 0 && firstLoad){
			console.log('loading index wrt @timestamp');
			$($('.kbn-settings-indices-create option')[1]).attr('selected','selected').trigger('change');
			$($('.kbn-settings-indices-create button')[0]).trigger('click');
			firstLoad = false;
			setTimeout(function(){
				window.location='/kibana/#/dashboard/DEFAULT-DASHBOARD';
			},5000)
		}
	},2000);

}
var intervalHandle = null;
var defaultDashboardLoaded = 0;
function main(){
	window.confirm = function () { return true }
	 setInterval(function(){
                $('#kibana-primary-navbar').css('background-color','#E3E3E3');
		$('.navbar-timepicker-container').css('background-color','#E3E3E3');
                $('.nav a').attr('style','background-color:#F0F0F0;color:#A39595');
                $('.nav .active a').attr('style','background-color:#959595;color:white');
                $('navbar').attr('style','background-color:#959595')

                $('.navbar-nav .logo').hide();
                $($('.nav li')[2]).hide();
                $($('.nav li')[4]).hide();
        },500);

	$(document).ready(function(){
		defaultColumns();
		intervalHandle = setInterval(function(){
                        if( defaultDashboardLoaded == 0 ){
                                defaultDashboardLoaded = defaultDashboardLoaded+1;
                                window.location='/kibana/#/dashboard/DEFAULT-DASHBOARD';
                        }
                },6000);
		setTimeout(function(){
			defaultDashboard();
			defaultIndex();
			defaultColumns();
		},5000);
	});
	setTimeout(function(){
		if ( $('.kbn-settings-indices-create option').length > 0 && firstLoad){
			$($('.kbn-settings-indices-create option')[1]).attr('selected','selected').trigger('change');
			$($('.kbn-settings-indices-create button')[0]).trigger('click');
			firstLoad = false;
		}
	},2500);
	$('body').click(function(){

		if($($('.nav li')[1]).hasClass('active')){
			if($($('.discover-selected-fields field-name')[0]).text() == 't_source'){
				setTimeout(function(){
					manipulateTimeStamp(changeCondensedTimestamp);
				},1000);
			}
		}
		$($('.nav li')[1]).click(function () {
			clearInterval(intervalHandle);
		});
		$($('.nav li')[3]).click(function () {
			setTimeout(function(){
				manipulateColor();
			},3000);
		});
		manipulateColor();
	});
	setInterval(function(){
		if(($('.discover-selected-fields field-name').text()).indexOf('@timestamp') >= 0){
			manipulateTimeStampFiltered();
		} }, 2000);
	setInterval(function(){
		manipulateColor();
	},2000);
}
main();
////////////////////////////////////////

