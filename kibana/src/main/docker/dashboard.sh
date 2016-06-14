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
#!/bin/sh


curl -H "Content-Type: application/json" --data @DEFAULT-INDEX.json elasticsearch_url/.kibana/config/4.1.4
curl -H "Content-Type: application/json" --data @INDEX-PATTERN.json elasticsearch_url/.kibana/index-pattern/logstash-*
curl -H "Content-Type: application/json" --data @LOG-SEVERITY.json elasticsearch_url/.kibana/visualization/LOG-SEVERITY
curl -H "Content-Type: application/json" --data @Parttype-logs.json elasticsearch_url/.kibana/search/Parttype-logs
curl -H "Content-Type: application/json" --data @severity-logs.json elasticsearch_url/.kibana/search/severity-logs
curl -H "Content-Type: application/json" --data @All-logs.json elasticsearch_url/.kibana/search/All-logs
curl -H "Content-Type: application/json" --data @platform-logs.json elasticsearch_url/.kibana/search/platform-logs
curl -H "Content-Type: application/json" --data @category-logs.json elasticsearch_url/.kibana/search/category-logs
curl -H "Content-Type: application/json" --data @Logs-category.json elasticsearch_url/.kibana/visualization/Logs-Category
curl -H "Content-Type: application/json" --data @LOGS-FOR-PLATFORM-NODES-AND-SERVICES.json elasticsearch_url/.kibana/visualization/LOGS-FOR-PLATFORM-NODES-AND-SERVICES
curl -H "Content-Type: application/json" --data @LOGS-OVER-TIME.json elasticsearch_url/.kibana/visualization/Logs-over-time
curl -H "Content-Type: application/json" --data @CASPIAN-SERVICES.json elasticsearch_url/.kibana/visualization/Logs-by-type
curl -H "Content-Type: application/json" --data @Caspian-components.json elasticsearch_url/.kibana/visualization/Neutrino-components
curl -H "Content-Type: application/json" --data @DEFAULT-DASHBOARD.json elasticsearch_url/.kibana/dashboard/DEFAULT-DASHBOARD
curl -H "Content-Type: application/json" --data @Audit_logs.json elasticsearch_url/.kibana/search/Audit_logs
curl -H "Content-Type: application/json" --data @Troubleshoot.json elasticsearch_url/.kibana/search/Troubleshoot
curl -H "Content-Type: application/json" --data @VM_logs.json elasticsearch_url/.kibana/search/VM_logs
curl -H "Content-Type: application/json" --data @TROUBLESHOOT-DASHBOARD.json elasticsearch_url/.kibana/dashboard/TROUBLESHOOT-DASHBOARD
curl -H "Content-Type: application/json" --data @CC_INSTANCES_DASHBOARD.json elasticsearch_url/.kibana/dashboard/CC_INSTANCES_DASHBOARD
curl -H "Content-Type: application/json" --data @AUDIT_DASHBOARD.json elasticsearch_url/.kibana/dashboard/AUDIT_DASHBOARD
curl -H "Content-Type: application/json" --data @Troubleshoot-logs-over-time.json elasticsearch_url/.kibana/visualization/Troubleshoot-logs-over-time
curl -H "Content-Type: application/json" --data @CC-logs-over-time.json elasticsearch_url/.kibana/visualization/CC-logs-over-time
curl -H "Content-Type: application/json" --data @CC-logs-components.json elasticsearch_url/.kibana/visualization/CC-logs-components
curl -H "Content-Type: application/json" --data @Audit-logs-by-severity.json elasticsearch_url/.kibana/visualization/Audit-logs-by-severity
curl -H "Content-Type: application/json" --data @Audit-logs-by-components.json elasticsearch_url/.kibana/visualization/Audit-logs-by-components
curl -H "Content-Type: application/json" --data @Troubleshoot-logs-components.json elasticsearch_url/.kibana/visualization/Troubleshoot-logs-components
curl -H "Content-Type: application/json" --data @CC-logs-by-severity.json elasticsearch_url/.kibana/visualization/CC-logs-by-severity
curl -H "Content-Type: application/json" --data @VM-logs-by-verb.json elasticsearch_url/.kibana/visualization/VM-logs-by-verb
curl -H "Content-Type: application/json" --data @Audit-logs-over-time.json elasticsearch_url/.kibana/visualization/Audit-logs-over-time
curl -H "Content-Type: application/json" --data @Troubleshoot-logs-severity.json elasticsearch_url/.kibana/visualization/Troubleshoot-logs-severity
curl -H "Content-Type: application/json" --data @Trouble-shoot-logs-by-type.json elasticsearch_url/.kibana/visualization/Trouble-shoot-logs-by-type
curl -H "Content-Type: application/json" --data @Audit-logs-by-type.json elasticsearch_url/.kibana/visualization/Audit-logs-by-type






