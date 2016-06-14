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
.PHONY: log-courier-rest prepare bin clean all


MAKEFILE := $(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
GOPATH := $(patsubst %/,%,$(dir $(abspath $(MAKEFILE))))
PWD := $(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
export GOPATH := $(GOPATH)
export PROJECT_NAME := LogCourierRestWrapper
ECLIPSE_PROJECT_FILE := .project

TAGS :=
BINS := bin/log-courier-rest

TAGS := $(TAGS) "github.com/ant0ine/go-json-rest/rest"
TAGS := $(TAGS) "github.com/revel/config"
TAGS := $(TAGS) "github.com/cihub/seelog"
TAGS := $(TAGS) "github.com/jasonlvhit/gocron"

all:  log-courier-rest

log-courier-rest: $(BINS)

clean:
	go clean -i ./...
	rm -rf src/github.com
	rm -rf pkg/linux_amd64/github.com
	rm -rf pkg/linux_amd64/log-courier-rest.a
	rm $(ECLIPSE_PROJECT_FILE)

eclipse:
	rm -rf pkg
	rm -rf bin
	mkdir -p pkg/linux_amd64
	mkdir -p bin/
	@echo '<?xml version="1.0" encoding="UTF-8"?>' > $(ECLIPSE_PROJECT_FILE)
	@echo "<projectDescription>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "  <name>$${PROJECT_NAME}</name>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "  <comment></comment>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "  <projects>" >>   $(ECLIPSE_PROJECT_FILE)
	@echo "  </projects>" >>  $(ECLIPSE_PROJECT_FILE)
	@echo "	 <buildSpec>" >>  $(ECLIPSE_PROJECT_FILE)
	@echo "	    <buildCommand>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "		<name>com.googlecode.goclipse.goBuilder</name>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "		<arguments>" >>   $(ECLIPSE_PROJECT_FILE)
	@echo "		</arguments>" >>  $(ECLIPSE_PROJECT_FILE)
	@echo "     </buildCommand>" >>   $(ECLIPSE_PROJECT_FILE)
	@echo "  </buildSpec>" >>         $(ECLIPSE_PROJECT_FILE)
	@echo "  <natures>" >>            $(ECLIPSE_PROJECT_FILE)
	@echo "	   <nature>com.googlecode.goclipse.core.goNature</nature>" >> $(ECLIPSE_PROJECT_FILE)
	@echo "  </natures>" >> 	 $(ECLIPSE_PROJECT_FILE)
	@echo "</projectDescription>" >> $(ECLIPSE_PROJECT_FILE)


test: 
	go test log-courier-rest 

prepare: 
	@go version >/dev/null || (echo "Go not found. You need to install Go version 1.4.2: http://golang.org/doc/install"; false)
	@go version | grep -q 'go version go1.[234]' || (echo "Go version 1.2-1.4, you have a version of Go that is not supported."; false)
	@echo "GOPATH: $${GOPATH}"

bin/%: prepare
	go get -d -tags "$(TAGS)" $*
	go install -tags "$(TAGS)" $*
