#!/bin/bash
target_dirs=`find src -type d -path "*/functions/*"`

for dir in ${target_dirs}; do
    cd "${dir}" || exit
    go mod init sample
    go mod tidy
    GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o bootstrap main.go
    zip $(basename ${dir}).zip bootstrap
    cd - || exit
done
