#!/bin/bash

gitRepo="/git/fwb-daten"

if [ ! -d $gitRepo ]; then
	cd /git
	git clone https://user:pass@github.com/subugoe/fwb-daten
fi