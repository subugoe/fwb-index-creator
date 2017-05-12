#!/bin/bash

gitRepo="/git/fwb-daten"

if [ -d "$gitRepo" ]; then
	rm -rf $gitRepo
fi

cd /git
git clone https://${GIT_USER}:${GIT_PASSWORD}@github.com/subugoe/fwb-daten
