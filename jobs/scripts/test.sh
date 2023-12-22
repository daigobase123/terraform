#!/bin/bash
v1_dirs=($(find v1 -type d -path "*/functions/*"))
echo ${v1_dirs[@]}

test=()

length1=${#v1_dirs[@]}
length2=${#test[@]}
echo $length1
echo $length2


if [ $length1 = 0 ] && [ $length2 = 0 ]; then
    echo "OK"
else
    echo "追加してください"
fi

# echo ${test[@]}

# v1_dirs=($(find ${WORKSPACE}/aws-lambda/v1 -type d -path "*/functions/*" | gawk -F/ '{print $NF}'))