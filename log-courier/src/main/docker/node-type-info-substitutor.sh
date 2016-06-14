#!/bin/bash


MyIpAddress=$CONTAINER_HOST_ADDRESS
CrsEndpoint=$COMPONENT_REGISTRY
node_type=$( $NODETYPE_GETTER_SCRIPT  $MyIpAddress $CrsEndpoint nodetype )
host_name=$( $NODETYPE_GETTER_SCRIPT  $MyIpAddress $CrsEndpoint hostname  )

SUBSTITUTE_NODETYPE_INFO()
{
   nodetype=$1
   sed -i "s#SERVICE_TYPE_NAME#${nodetype}#" "${LCREST_SETTNGS_CONF_FILE}"
}

SUBSTITUTE_HOSTNAME_INFO()
{
   hostname=$1
   sed -i "s#HOST_NAME#${hostname}#" "${LCREST_SETTNGS_CONF_FILE}"
}


$( SUBSTITUTE_NODETYPE_INFO $node_type )
$( SUBSTITUTE_HOSTNAME_INFO $host_name )
