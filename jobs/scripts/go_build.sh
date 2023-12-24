#!/bin/bash
target_dirs=`find src -type d -path "*/functions/*"`

# go install
# go mod tidy
# cd -


for dir in ${target_dirs}; do
    # echo "${dir}/main.go"
    GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o "${dir}/bootstrap" "${dir}/main.go"
    zip "${dir}/$(basename ${dir}).zip" "${dir}/bootstrap"
done
