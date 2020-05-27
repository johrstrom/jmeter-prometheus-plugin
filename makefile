.DEFAULT_GOAL := help
SHELL := /bin/bash
THIS_FILE := $(lastword $(MAKEFILE_LIST))
THIS_FOLDER := $(shell basename $(CURDIR))

#Service version, tag and image name
master_version := $$(git show master:pom.xml | xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" -)
this_version := $$(git show master:pom.xml | xmllint --xpath "//*[local-name()='project']/*[local-name()='version']/text()" -)
this_branch := $(shell git rev-parse --abbrev-ref HEAD)
repo_location := $(strip $(shell  git rev-parse --show-toplevel))
clean ?= false

ifeq ($(strip $(clean)),true)
CLEAN := clean
endif

.PHONY: help
help:					## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
	
.PHONY: clean
clean:              ## Cleans the repo
	@mvn clean

.PHONY: build
build:              ## Builds jar
	@mvn install -Dgpg.skip

.PHONY: test
test:              ## run tests
	@mvn verify -Dgpg.skip


.PHONY: info
info:    					## Print some info on the repo
	@echo "this_version: $(this_version)" && \
	echo "this_branch: $(this_branch)" && \
	echo "repo_location: $(repo_location)" &&\
	echo "master_version: $(master_version)"
	