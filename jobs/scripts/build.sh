#!/bin/bash
target_dirs=`find v1 -type d -path "*/functions/*" -not -name "build"`

for dir in ${target_dirs}
do
    if [ ! -d "${dir}/build" ]; then
        mkdir "${dir}/build"
    fi
    zip "${dir}/build/${dir##*/}.zip" -r $dir  
done