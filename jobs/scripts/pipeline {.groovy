pipeline {
    agent any
    environment {
        work_dir='/home/jenkins/work'
        bundle='/home/jenkins/bin/bundle'
        deploy_dir='/home/jenkins/deploy'
        aws_credential = "aws_credential"
        git_credential = "git_credential"
        
        gf_base_path="${workspace}/gf/aws-lambda"
        yos_base_path="${workspace}/tenant/aws-lambda"
        
       
        gf_v1_path="${gf_base_path}/v1"
        gf_v2_path="${gf_base_path}/src"
        terraform_path="${gf_base_path}/jobs/terraform"
        build_path="${gf_base_path}/jobs/scripts/build.sh"
        yos_path="${yos_base_path}/src"
        
        gf_except="sample1,delete"
        yos_except="v2sample1"
    }
    tools { go '1.19' }
    stages {
        stage("checkout") {
            steps {
                cleanWs()
                dir("gf/aws-lambda") {
                    checkout([$class: 'GitSCM', 
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[credentialsId: "git_credential", url: 'https://github.com/daigobase123/terraform.git']]
                    ])
                }
                 dir("tenant/aws-lambda") {
                    checkout([$class: 'GitSCM', 
                    branches: [[name: '*/test']],
                    userRemoteConfigs: [[credentialsId: "git_credential", url: 'https://github.com/daigobase123/terraform.git']]
                    ])
                }
            }
        }
        stage(conflict){
            steps {
                sh """
                chmod +x "${build_path}"
                sh "${build_path}"
                """
            }
        }
    }
}
