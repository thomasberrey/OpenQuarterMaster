#!/bin/bash

oc new-project quartermaster

pushd yaml/openshift
./install.sh

oc patch servicemeshmemberroll/default -n istio-system --type=merge -p '{"spec": {"members": ["quartermaster"]}}'


