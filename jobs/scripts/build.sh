# #!/bin/bash

# gf_except="sample1,delete"
# yos_except="v2sample1"

# gf_base_path="${workspace}/gf/aws-lambda"
# yos_base_path="${workspace}/tenant/aws-lambda"

# gf_v1_path="v1"
# gf_v2_path="src"
# terraform_path="jobs/terraform"
# build_path="jobs/scripts/build.sh"
# yos_path="v2"

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
# echo $gf_except
# echo $yos_except

# gf lambda function build target 
gf_lambda_v1=($(find ${gf_v1_path} -type d -path "*/functions/*" -printf "%f\n"))
gf_lambda_v1_diff_dirs=($(find ${gf_v1_path} -type d -path "*/functions/*" | grep -vFf <(printf "%s\n" "${gf_except[@]}")))
gf_lambda_v1_diff=(`difference "${gf_lambda_v1[*]}" "${gf_except[*]}"`)



gf_lambda_v2=($(find ${gf_v2_path} -type d -path "*/functions/*" -printf "%f\n"))
gf_lambda_v2_diff=(`difference "${gf_lambda_v2[*]}" "${gf_except[*]}"`)

# 環境変数のexceptを除いたlmabda function
gf_lambda_function=(`union "${gf_lambda_v1_diff[*]}" "${gf_lambda_v2_diff[*]}"`)

# gf v1 v2の関数名の衝突
gf_intersection_array=(`intersection "${gf_lambda_v1_diff[*]}" "${gf_lambda_v2_diff[*]}"`)
echo "${gf_intersection_array[@]}"

# yos lambda function build target 
yos_lambda=($(find ${yos_path} -type d -path "*/functions/*" -printf "%f\n"))
yos_lambda_function=(`difference "${yos_lambda[*]}" "${yos_except[*]}"`)

# gf yosの関数名の衝突
gf_yos_intersection_array=(`intersection "${gf_lambda_function[*]}" "${yos_lambda_function[*]}"`)


if [ ${#gf_intersection_array[*]} = 0 ] && [ ${#gf_yos_intersection_array[*]} = 0 ]; then
   # gf v1 build
   echo "gf v1| ${gf_lambda_v1_diff[@]}"
   for dir in ${gf_lambda_v1_diff_dirs[@]};
   do
        if [ -d ${dir} ]; then
            echo ${dir}
            zip "${dir}/$(basename ${dir}).zip" -r "${dir}"
        fi
   done

   # gf v2 build 
   echo "gf v1| ${gf_lambda_v2_diff[@]}"
   
   cd $gf_base_path
   for dir in ${gf_lambda_v2_diff[@]};
   do
        if [ -d "${gf_v2_path}/functions/${dir}" ]; then
            GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o "${gf_v2_path}/functions/${dir}/bootstrap"    "${gf_v2_path}/functions/${dir}/main.go"
            zip "${gf_v2_path}/functions/${dir}/${dir}.zip" "${gf_v2_path}/functions/${dir}/bootstrap" 
        fi
   done
   cd -

    # yos build 
   echo "yos| ${yos_lambda_function[@]}"
   cd $yos_base_path
   for dir in ${yos_lambda_function[@]};
   do
        if [ -d "${yos_path}/functions/${dir}" ]; then
            GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o "${yos_path}/functions/${dir}/bootstrap"    "${yos_path}/functions/${dir}/main.go"
            zip "${yos_path}/functions/${dir}/${dir}.zip" "${yos_path}/functions/${dir}/bootstrap" 
        fi
   done
   cd -

   # terrafrom zip
    zip "${terraform_path}/$(basename ${terraform_path}).zip" -r "${terraform_path}"
   
else
    echo "環境変数に ${gf_intersection_array} ${gf_yos_intersection_array}を追加するかlambda function名を変更してください"
    exit 1
fi


