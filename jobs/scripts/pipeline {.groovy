pipeline {
    agent any
    tools {
        go 'Go1.2'
    }
    environment {
        work_dir='/home/jenkins/work'
        bundle='/home/jenkins/bin/bundle'
        deploy_dir='/home/jenkins/deploy'
        aws_crdenatial = "aws"
        git_crdenatial = "git"
        gf_except="delete,dynamo_aboition_pubresvitemgroup"
        yos_except="sample1"
    }
    stages {
        stage("git-v1") {
            steps {
                cleanWs()
                dir("aws-lambda") {
                    checkout([$class: 'GitSCM', 
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[credentialsId: "${git_crdenatial}", url: 'https://github.com/daigobase123/terraform.git']]
                    ])
                }
            }
        }
        stage("git-v2") {
             steps {
                dir("tenant/aws-lambda") {
                    checkout([$class: 'GitSCM', 
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[credentialsId: "${git_crdenatial}", url: 'https://github.com/daigobase123/terraform.git']]
                    ])
                }
            }
        }
            
        stage("aws-lambda-v1 build") {
            steps {
                 script {
                    sh '''
                        target_dirs=`find ${WORKSPACE}/aws-lambda/v1 -type d -path "*/functions/*"`
                        for dir in ${target_dirs}; do
                            zip "${dir}/$(basename ${dir}).zip" -r "${dir}"
                        done
                    '''
                }
            }
        }
        stage("aws-lambda-v2 build") {
            steps {
                script { 
                    sh '''
                        target_dirs=`find ${WORKSPACE}/tenant/aws-lambda/src -type d -path "*/functions/*"`

                        for dir in ${target_dirs}; do
                            cd "${dir}" || exit
                          
                            GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o bootstrap main.go
                            zip $(basename ${dir}).zip bootstrap
                            cd - || exit
                        done
                    '''
                }
            }
        }
        stage("s3artifac") {
            steps {
                script {
                    sh '''#!/bin/bash
                        intersection() {
                            printf '%s\n' $@ | sort | uniq -d
                        }
                         difference() {
                            (printf '%s\n' $@ | sort -u; printf '%s\n' ${2}) | sort | uniq -u
                        }
                        v1_dirs=($(find ${WORKSPACE}/aws-lambda/v1 -type d -path "*/functions/*" | gawk -F/ '{print $NF}'))
                        v2_dirs=($(find ${WORKSPACE}/tenant/aws-lambda/src -type d -path "*/functions/*" | gawk -F/ '{print $NF}'))
                        
                        gf_except_array=(${gf_except//,/ })
                        yos_except_array=(${yos_except//,/ })
                        intersection_array=(`intersection "${v1_dirs[*]}" "${v2_dirs[*]}"`)
                        echo "intersection"
                        echo ${intersection_array[@]}
    
                        difference_array1=(`difference "${intersection_array[*]}" "${gf_except[*]}"`)
                        echo "difference (array1 - array2)"
                        echo ${difference_array1[@]}
    
                        difference_array2=(`difference "${intersection_array[*]}" "${yos_except[*]}"`)
                        echo "difference (array2 - array1)"
                        echo ${difference_array2[@]}
                        
                        if [ ${#difference_array1[@]} = 0 ] && [ ${#difference_array2[@]} = 0 ]; then
                            echo "OK"
                            difference_array3=(`difference "${v1_dirs[*]}" "${gf_except[*]}"`)
                            echo "difference (array1 - array2)"
                            for r in ${difference_array3[@]}:
                            do
                                echo $(find ${WORKSPACE}/aws-lambda/v1 -type f -name ${r}.zip)
                                aws s3 cp  $(find ${WORKSPACE}/aws-lambda/v1 -type f -name ${r}.zip) s3://lambda-terasako/v1
                            done
                            difference_array4=(`difference "${v2_dirs[*]}" "${yos_except[*]}"`)
                            echo "difference (array2 - array1)"
                            echo ${difference_array4[@]}
                            for r in ${difference_array4[@]}:
                            do
                                echo $(find ${WORKSPACE}/aws-lambda/src -type f -name ${r}.zip)
                                aws s3 cp  $(find ${WORKSPACE}/aws-lambda/v1 -type f -name ${r}.zip) s3://lambda-terasako/v2
                            done
                        else
                            echo "環境変数fg_except"${difference_array1[@]}"を追加してください"
                            echo "環境変数yos_except"${difference_array2[@]}"を追加してください"
                            exit 1
                        fi

                    '''
                }
            }
        }
    }
  
    post {
        failure {
            // ジョブが失敗した場合の処理
            echo 'This will run only if the stage fails'
        }
        success {
            // ジョブが成功した場合の処理
            echo 'This will run only if the stage succeeds'
        }
    }
}