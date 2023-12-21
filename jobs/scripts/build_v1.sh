#!/bin/bash

# gf-aws-lambda-v1-build(python)
# target_dirs=`find v1 -type d -path "*/functions/"`
target_dirs=`find v1 -type d -path "*/functions/*"`

for dir in ${target_dirs}; do
    zip "${dir}/$(basename ${dir}).zip" -r "${dir}"
done