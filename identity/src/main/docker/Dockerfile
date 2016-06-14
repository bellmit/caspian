#Copyright (c) 2015 EMC Corporation
# All Rights Reserved
#
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#

#
# Caspian Common Service : OpenStack Keystone Image
#TODO: add version and specific packages similar to earlier Dockerfile

FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35
MAINTAINER thomas3

RUN zypper -n ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common

RUN zypper --non-interactive --quiet --no-gpg-checks addrepo http://asdrepo.isus.emc.com:8081/artifactory/caspianCC-local/Mitaka-05_26_16.repo
RUN zypper --non-interactive --quiet --no-gpg-checks ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCC-local/common-mitaka-master.repo
RUN zypper mr -p 1 Mitaka-05_26_16
RUN zypper mr -p 2 common-mitaka-master

RUN zypper -n ar http://asdrepo.isus.emc.com:8081/artifactory/caspian-build ccsbuild

RUN zypper --no-gpg-checks refresh
RUN zypper -n install rsyslog-8.4.0-5.1 sysconfig-0.83.8-7.1 sysconfig-netconfig-0.83.8-7.1 syslog-service-2.0-778.1 timezone-2015c-0.17.1 wicked-0.6.18-16.1 wicked-service-0.6.18-16.1
RUN zypper -n install python-ipaddress
RUN zypper -n --no-gpg-checks refresh
RUN zypper -n install openstack-keystone
RUN zypper -n install gcc-4.8-6.189 python-devel-2.7.9-14.1

RUN zypper -n install apache2-2.4.10-14.10.1 apache2-prefork-2.4.10-14.10.1 \
    apache2-mod_wsgi-4.4.6-1.1 apache2-utils-2.4.10-14.10.1 apache2-prefork-2.4.10-14.10.1 \
    apache2-mod_nss-1.0.8-9.32 \
    python-mysql-1.2.4-1.38 \
    curl-7.37.0-15.1

RUN easy_install http://asdrepo.isus.emc.com:8081/artifactory/caspian-build/com/emc/caspian/encryption-python/1.0.0.1.2394.fb26b55/encryption-python-1.0.0.1.2394.fb26b55.tar.gz

# Removing the gcc compilers and devel packages
RUN zypper -n remove python-devel gcc gcc48 cpp cpp48 

#TODO: To add these packages to the new caspianCS-v1.1.0 local repo.
# Installing saml packages

RUN zypper -n install libltdl7-2.4.2-14.60 tack-5.9-40.124 libncurses6-5.9-40.124 \
    ncurses-devel-5.9-40.124 readline-devel-6.2-75.2 xz-devel-5.0.5-4.852 \
    zlib-devel-1.2.8-5.1 libxml2-tools-2.9.1-17.1 libxml2-devel-2.9.1-17.1 \
    libgpg-error-devel-1.13-1.79 libgcrypt-devel-1.6.1-16.1 \
    libxslt-tools-1.1.28-6.57 libxslt-devel-1.1.28-6.57 libxmlsec1-1-1.2.18-1.6 \
    libxmlsec1-devel-1.2.18-1.6 libopenssl-devel-1.0.1i-17.1 \
    libxmlsec1-openssl1-1.2.18-1.6 libxmlsec1-openssl-devel-1.2.18-1.6 xmlsec1-1.2.18-1.6

COPY wsgi-keystone.conf  /etc/apache2/conf.d/wsgi-keystone.conf
COPY policy.json /etc/keystone/policy.json
RUN sed -i "s#^Include /etc/apache2/sysconfig.d/include.conf.*##" /etc/apache2/httpd.conf
RUN sed -i "s#.*Require all denied#     Require all granted#" /etc/apache2/httpd.conf
RUN sed -i "s#.*su keystone keystone#    su root root#" /etc/logrotate.d/openstack-keystone

RUN echo "LoadModule wsgi_module    /usr/lib64/apache2/mod_wsgi.so" >> /etc/apache2/sysconfig.d/loadmodule.conf
RUN echo "LoadModule nss_module     /usr/lib64/apache2/mod_nss.so" >> /etc/apache2/sysconfig.d/loadmodule.conf
RUN echo "LoadModule version_module    /usr/lib64/apache2/mod_version.so" >> /etc/apache2/sysconfig.d/loadmodule.conf

RUN mkdir -p /data && chown keystone:keystone -R /data
RUN mkdir -p /etc/keystone && chown keystone:keystone -R /etc/keystone
RUN mkdir -p /var/log/keystone && chown keystone:keystone -R /var/log/keystone
RUN mkdir -p /etc/keystone/scripts && chown keystone:keystone -R /etc/keystone/scripts

COPY keystone.conf.template /etc/keystone/keystone.conf.template
RUN mkdir -p /etc/keystone/ssl && \
   mkdir -p /etc/keystone/domains
RUN chown keystone:keystone -R /etc/keystone/ssl && \
    chown keystone:keystone -R /etc/keystone/domains
COPY ssl  /etc/keystone/ssl

COPY apache2 /etc/apache2
COPY scripts /etc/keystone/scripts
RUN echo "ServerSignature Off" >> /etc/apache2/httpd.conf
RUN echo "ServerTokens Prod" >> /etc/apache2/httpd.conf
COPY startup.sh /etc/startup.sh
RUN chmod +x /etc/startup.sh
RUN chmod +x /etc/keystone/scripts/TokenFlush.py
RUN chmod +x /etc/keystone/scripts/CronJobs.sh

# Run the cron job every hour at 0th minute
RUN echo "0 */1 * * * /etc/keystone/scripts/CronJobs.sh" >> /var/spool/cron/tabs/root

# Removing the zypper repos
RUN zypper rr caspiancs_common

EXPOSE 6100 35357

ENTRYPOINT ["/etc/startup.sh"]




