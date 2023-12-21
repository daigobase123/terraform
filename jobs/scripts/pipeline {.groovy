pipeline {
    agent any
    environment {
        work_dir='/home/jenkins/work'
        bundle='/home/jenkins/bin/bundle'
        deploy_dir='/home/jenkins/deploy'
        aws_crdenatial = "aws"
        git_crdenatial = "git"
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
                            go mod init sample
                            go mod tidy
                            GOOS=linux GOARCH=amd64 go build -tags lambda.norpc -o bootstrap main.go
                            zip $(basename ${dir}).zip bootstrap
                            cd - || exit
                        done
                    '''
                }
            }
        }
        // stage("s3artifac") {
        //     steps {
        //         script {
        //             def fileToUpload =  "${WORKSPACE}/aws-lambda/v1/delete/functions/delete/delete.zip" // アップロードするファイルのパス
        //             def bucketName = 'lambda-terasako' // S3バケット名
        //             def v1 = 'v1' // バージョン識別子

        //             sh """
        //             aws s3 cp ${fileToUpload} s3://${bucketName}/${v1}/
        //             """
        //         }
        //     }
        // }
    }
}