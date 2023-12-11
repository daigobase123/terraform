#!/bin/sh
test=`find functions -name main.go -type f`

echo "Hello, World!"
base_path=`pwd`
for main_program_path in $test
do
  build_dir=`basename ${main_program_path%/*}`
  echo $build_dir
  echo "${base_path%/}/`dirname $main_program_path`"
  cd "${base_path%/}/`dirname $main_program_path`"
  GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -tags lambda.norpc -o bootstrap main.go
  zip $build_dir.zip bootstrap
  aws s3 cp $build_dir.zip s3://lambda-terasako
done