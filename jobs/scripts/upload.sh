#!/bin/bash
# array=($(basename $(find src -type d -path "*/functions/*")))
# find コマンドでディレクトリの一覧を取得して配列に格納
intersection() {
    printf '%s\n' $@ | sort | uniq -d
}

# 差集合 (1番目の引数 - 2番目の引数)
difference() {
        (printf '%s\n' $@ | sort -u; printf '%s\n' ${2}) | sort | uniq -u
}

# find の結果を配列に格納
array1=($(find v1 -type d -path "*/functions/*" -exec basename {} \;))
array2=($(find src -type d -path "*/functions/*" -exec basename {} \;))

gf_except=("delete","dynamo_aboition_pubresvitemgroup")
yos_except=("sample1")
# union_array=(`union "${array1[*]}" "${array2[*]}"`)
# echo "union"
# echo ${union_array[@]}
echo ${array1[@]}
echo ${array2[@]}

intersection_array=(`intersection "${array1[*]}" "${array2[*]}"`)
echo "intersection"
echo ${intersection_array[@]}

difference_array1=(`difference "${intersection_array[*]}" "${gf_except[*]}"`)
echo "difference (array1 - array2)"
echo ${difference_array1[@]}

difference_array2=(`difference "${intersection_array[*]}" "${yos_except[*]}"`)
echo "difference (array2 - array1)"
echo ${difference_array2[@]}

difference_array3=(`difference "${array1[*]}" "${gf_except[*]}"`)
echo "difference (array1 - array2)"
# echo "v1/${difference_array3[@]}/functions/"
for r in ${difference_array3[@]}:
do
    echo $(find v1 -type f -name ${r}.zip)
    # aws s3 cp  $(find v1 -type f -name ${r}.zip) http://s3/lambda-terasako/v1
done

difference_array4=(`difference "${array2[*]}" "${yos_except[*]}"`)
echo "difference (array2 - array1)"
echo ${difference_array4[@]}
echo "環境変数fg_exceptに"${difference_array4[@]}"を追加してください"
