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
.PHONY: log-courier-bootstraper prepare bin clean all


MAKEFILE := $(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
GOPATH := $(patsubst %/,%,$(dir $(abspath $(MAKEFILE))))
export GOPATH := $(GOPATH)

TAGS :=
BINS := bin/log-courier-bootstrapper

TAGS := $(TAGS) "github.com/revel/config"

all:  log-courier-bootstrapper

log-courier-bootstrapper: $(BINS)

clean:
	go clean -i ./...
	rm -rf src/github.com
	rm -rf pkg/linux_amd64/github.com
	rm -rf pkg/linux_amd64/log-courier-bootstrapper.a

prepare: 
	@go version >/dev/null || (echo "Go not found. You need to install Go version 1.4.2: http://golang.org/doc/install"; false)
	@go version | grep -q 'go version go1.[234]' || (echo "Go version 1.2-1.4, you have a version of Go that is not supported."; false)
	@echo "GOPATH: $${GOPATH}"

bin/%: prepare
	go get -d -tags "$(TAGS)" $*
	go install -tags "$(TAGS)" $*