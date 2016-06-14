#!/bin/bash
export OS_AUTH_URL=https://keystone:35357/v3 
export OS_USERNAME=admin 
export OS_PASSWORD=admin123
export OS_DOMAIN_NAME=default 
export OS_IDENTITY_API_VERSION=3
export ACCOUNT_AUTH_URL=http://account:35359/accounts/

alias account='python client_cli.py'

