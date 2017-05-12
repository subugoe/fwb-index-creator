#!/bin/bash

gitRepo="/git/fwb-daten"

if [ ! -d "$gitRepo" ]; then
	cd /git
	git clone https://${GIT_USER}:${GIT_PASSWORD}@github.com/subugoe/fwb-daten
fi