#!/bin/bash

gf_except="sample1"
yos_except="v2sample1"

# 和集合
union() {
        printf '%s\n' $@ | sort | uniq
}
# find コマンドでディレクトリの一覧を取得して配列に格納
intersection() {
    printf '%s\n' $@ | sort | uniq -d
}

# 差集合 (1番目の引数 - 2番目の引数)
difference() {
        (printf '%s\n' $@ | sort -u; printf '%s\n' ${2}) | sort | uniq -u
}

# 環境変数を配列にパース
gf_except=($(echo "$gf_except" | tr ',' '\n')) 
yos_except=($(echo "$yos_except" | tr ',' '\n')) 

# gf lambda function build target 
gf_lambda_v1=($(find v1 -type d -path "*/functions/*" -printf "%f\n"))
gf_lambda_v1_diff=(`difference "${gf_lambda_v1[*]}" "${gf_except[*]}"`)

gf_lambda_v2=($(find src -type d -path "*/functions/*" -printf "%f\n"))
gf_lambda_v2_diff=(`difference "${gf_lambda_v2[*]}" "${gf_except[*]}"`)

# 環境変数のexceptを除いたlmabda function
gf_lambda_function=(`union "${gf_lambda_v1_diff[*]}" "${gf_lambda_v2_diff[*]}"`)

# gf v1 v2の関数名の衝突
gf_intersection_array=(`intersection "${gf_lambda_v1_diff[*]}" "${gf_lambda_v2_diff[*]}"`)


# yos lambda function build target 
yos_lambda=($(find v2 -type d -path "*/functions/*" -printf "%f\n"))
yos_lambda_function=(`difference "${yos_lambda[*]}" "${yos_except[*]}"`)

# gf yosの関数名の衝突
gf_yos_intersection_array=(`intersection "${gf_lambda_function[*]}" "${yos_lambda_function[*]}"`)



if [ ${#gf_intersection_array[*]} = 0 ] && [ ${#gf_intersection_array[*]} = 0 ]; then
   echo "OK"
   echo "gf | ${gf_lambda_function[@]}"
   echo "yos| ${yos_lambda_function[@]}"
else
    exit 1
fi
