#!/usr/bin/python


import requests;
import argparse


class Node:
    ip = ""
    node_type=""
    hostname=""

    def __init__(self, ipv4, node_type, hostname):
        self.ip = ipv4
        self.node_type = node_type
        self.hostname = hostname

    def equals(self, ipv4):
        if (self.ip == ipv4 or self.ip == "255.255.255.255"):
            return True;
        else:
            return False;

    def getNodeType(self):
        return self.node_type;


    def getHostname(self):
        return self.hostname;



class NodeInfoMediator:
      nodelist = []
      services = []
      node_inventory_endpoint = "";

      def __init__(self, component_registry):
          self.component_registry_basepath = component_registry;
          self.component_registry_servicepath = "/v1/services";
          self.node_inventory_info_relativepath = "/v1/services/platform/components/node-inventory";
          self.node_inventory_nodeinfo_service = "/v1/nodes/allocation?service=";
          self.node_inventory_endpoint = "";
          self.getServiceListFromComponentRegistry();
          self.getNodeInventoryEndpointFromComponentRegistry();
          self.fetchNodeInformation();


      def fetchNodeInformation(self):
           services = self.services;
           for service in services:
               node_type = service;
               infolist = self.getNodeInfoFromNodeInventory(service)
               for info in infolist:
                  ip = info['ipv4']
                  hostname = info['hostname']
                  if ( ip != ""):
                    if( ip == "255.255.255.255"):
                         node_type = "platform"

                  self.nodelist.append(Node(ip, node_type, hostname))


      def getServiceListFromComponentRegistry(self):
          url = self.component_registry_basepath + self.component_registry_servicepath;
          try:
             response = requests.get(url);
             if (response.status_code == 200):
                if (response.json() != ""):
                    for service in response.json()['services']:
                             self.services.append(service['service'])
          except Exception:
              self.services.append('platform');

          return self.services


      def getNodeInfoFromNodeInventory(self, service):
          url = self.node_inventory_endpoint + self.node_inventory_nodeinfo_service + service
          infolist = []
          try:
            response = requests.get(url);
            if (response.status_code == 200):
                 if (response.json() != "" and len(response.json()['nodes']) != 0):
                             for node in response.json()['nodes']:
                                 info = {}
                                 if (node['topology']['external_ipv4'] != ""):
                                    info['ipv4'] = node['topology']['external_ipv4']
                                 if (node['topology']['hostname'] != ""):
                                     info['hostname'] = node['topology']['hostname'];
                                 infolist.append(info)
          except Exception:
                 info = {}
                 info['ipv4'] = "255.255.255.255"
                 info['hostname'] = "Caspian"
                 infolist.append(info)
          return infolist


      def getNodeInventoryEndpointFromComponentRegistry(self):
          self.node_inventory_endpoint = ""
          generatedurl = ""
          url = self.component_registry_basepath + self.node_inventory_info_relativepath;
          response = requests.get(url);
          if (response.status_code == 200):
              if(response.json() != "" and len(response.json()['balance']) != 0 and response.json()['balance'][0]['endpoint_type'] == "private_backend" and response.json()['balance'][0]['vip'] != ""):
                      self.node_inventory_endpoint = response.json()['balance'][0]['scheme'] + "://" + response.json()['balance'][0]['vip'] + ":" +  response.json()['balance'][0]['port']
              elif (response.json() != "" and len(response.json()['endpoints']) != 0 ):
                     self.node_inventory_endpoint = response.json()['endpoints'][0]['url']
          return self.node_inventory_endpoint



      def getNodeType(self, ipv4):
          node_type = "platform"
          for node in self.nodelist:
              if (node.equals(ipv4)):
                 node_type = node.getNodeType();
                 break;
          return node_type;


      def getHostname(self, ipv4):
           hostname = "Caspian"
           for node in self.nodelist:
               if (node.equals(ipv4)):
                  hostname = node.getHostname();
                  break;
           return hostname;


parser = argparse.ArgumentParser()
parser.add_argument("ip", help="ip address of the system whose nodetype we want to know")
parser.add_argument("component_registry", help="component registry endpoint url")
parser.add_argument("command", help="information type that is needed for a node [nodetype|hostname]")
args = parser.parse_args()
node_info_mediator = NodeInfoMediator(args.component_registry);

if (args.command == "nodetype"):
    print node_info_mediator.getNodeType(args.ip)

if (args.command == "hostname"):
    print node_info_mediator.getHostname(args.ip)
