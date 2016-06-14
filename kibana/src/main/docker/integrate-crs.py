
#
# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
#
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#

import os
import requests
import time
import sys
import re

def getResponse(component):
        p =  requests.get(os.environ['COMPONENT_REGISTRY']+'/v1/services/platform/components/' + component)
        return p

regEx = "^((http[s]?|ftp):\/)?\/?([^:\/\s]+):\)?([\w\-\.]+[^#?\s]+)(\?([^#]*))?(#(.*))?$"

def parsePort(url):
        urlParts = re.compile(regEx).split(url)
    	return urlParts[4]

def parseScheme(url):
        urlParts = re.compile(regEx).split(url)
        return urlParts[2]

def getKeystoneUrl():
        vip = getVipFromKeystone()
        if(vip!=""):
                var =1
                while (var):
                        r = getResponse('keystone')
                        if(r.status_code==200):
                                resp = r.json()
                                for i,val in enumerate( resp['balance']):
                                        if(len(val)!=0):
                                                if(val['path']=='/' and val['endpoint_name']=='admin'):
                                                        ip = val['vip']
                                                        port = val['port']
                                                        scheme = val['scheme']
                                                        url = scheme + '://' + ip+":" + port
                                                        return url
                                                        break
                        else:
                                time.sleep(5)

        else:
                url = getPrivateUrl('keystone')
                return url


def getElasticsearchUrl():
        vip = getVipFromKeystone()
        if(vip!=""):
                testUrl=getPublicUrl('elasticsearch')
                scheme = parseScheme(testUrl)
                port = parsePort(testUrl)
                url = scheme+"://"+vip+":"+port
                return url
        else:
                url = getPublicUrl('elasticsearch')
                return url

def getExportUrl():
        vip=getVipFromKeystone()
        if(vip!=''):
                testUrl=getExportPublicUrl()
                scheme=parseScheme(testUrl)
                port=parsePort(testUrl)
                url=scheme+"://"+vip+":"+port
                return url
        else:
                url=getExportPublicUrl()
                return url

def getExportDownloadUrl():
	vip=getVipFromKeystone()
	if(vip!=''):
		url="https://"+vip+"/kibana/api/logs/archive/"
		return url
	else:
		exportUrl=getExportPublicUrl()
		url=exportUrl+"/api/logs/archive/"
		return url


def getExportPublicUrl():
        var = 1
        while (var):
                r = getResponse('elasticsearch')
                if(r.status_code==200):
                        resp = r.json()
                        for i,val in enumerate(resp['endpoints']):
                            if(len(val)!=0):
                                if(val['name']=='EXPORT'):
                                        url = val['url']
                                        return url
                                        break
                else:
                        time.sleep(5)

def getPrivateUrl(component):
        var = 1
        while (var):
                r = getResponse(component)
                if(r.status_code==200):
                        resp = r.json()
                        for i,val in enumerate(resp['endpoints']):
                            if(len(val)!=0):
                                if(val['type']=='private'):
                                        url = val['url']
                                        return url
                                        break
                else:
                        time.sleep(5)

def getPublicUrl(component):
        var = 1
        while (var):
                r = getResponse(component)
                if(r.status_code==200):
                        resp = r.json()
                        for i,val in enumerate(resp['endpoints']):
                            if(len(val)!=0):
                                if(val['type']=='public' and val['name']!='EXPORT'):
                                        url = val['url']
                                        return url
                                        break
                else:
                        time.sleep(5)

def getVipFromKeystone():
        var = 1
        while (var):
                r = getResponse('keystone')
                if(r.status_code==200):
                        resp = r.json()
                        if(len(resp['balance'])!=0):
                                vip = resp['balance'][0]['vip']
                                return vip
                                break
                else:
                        time.sleep(5)


if(sys.argv[1]=='keystone'):
        print getKeystoneUrl()
elif(sys.argv[1]=='elasticsearch'):
        print getElasticsearchUrl()
elif(sys.argv[1]=='export'):
        print getExportUrl()
elif(sys.argv[1]=='export_download'):
	print getExportDownloadUrl();
