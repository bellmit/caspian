#!/bin/bash

script_parent_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
logpath_pattern_conf_file="${script_parent_dir}/logpath-pattern.conf"


PARSE_CONF_FILE()
{
 confpattern=$1
 
 touch .parseconf
 cat $logpath_pattern_conf_file | grep $confpattern |  awk -F "=" '{for (i=2; i<=NF; i++) print $i" "}' | awk -F "," '{for (i=1; i<=NF; i++) print $i" "}' | awk  '{for (i=1; i<=NF; i++) print $i" "}' > .parseconf
 parseditems=`tr '\n' ' ' < .parseconf | rev | cut -c 2- | rev`
 rm .parseconf
 
 echo "${parseditems[@]}"
}


COLLECT_LOGFILES()
{

 logpath_patterns=$1
 logpath_dirs=$2
 
 logpath_dir_pattern=$3;
 touch .aggregatedlogpaths
 for logpath_dir in $logpath_dirs
 do
      if [[ ! $logpath_dir_pattern = "" ]]
      then
         logpath_dir_pattern="-wholename *$logpath_dir_pattern*"
      fi
 
      if [ -d $logpath_dir ]; then
       for logpath_pattern in $logpath_patterns
       do
         find $logpath_dir $logpath_dir_pattern -name $logpath_pattern >> .aggregatedlogpaths
       done
      fi
 done
 
 logpaths=`tr '\n' ',' < .aggregatedlogpaths | rev | cut -c 2- | rev`
 rm .aggregatedlogpaths
 
 echo $logpaths
}

logpath_patterns=$(PARSE_CONF_FILE "logpath_patterns")
logpath_dirs=$(PARSE_CONF_FILE "logpath_dirs")

if [ "$#" -eq 1 ] ; then
  if [ "$1" == "_all" ]; then
   logpath_dir_pattern= "";
  else
   logpath_dir_pattern=$1
  fi
fi


LOG_PATHS=$(COLLECT_LOGFILES "${logpath_patterns[@]}" "${logpath_dirs[@]}" "$logpath_dir_pattern")

echo "$LOG_PATHS"
