#!/bin/bash

oc new-project quartermaster

pushd yaml/openshift
./install.sh

oc patch servicemeshmemberroll/default -n istio-system --type=merge -p '{"spec": {"members": ["quartermaster"]}}'

oc patch route/istio-ingressgateway -n istio-system --type=merge -p '{"spec": {"tls": {"termination": "edge"}}}'
oc patch route/istio-ingressgateway -n istio-system --type=merge -p '{"spec": {"tls": {"insecureEdgeTerminationPolicy": "Redirect"}}}'

