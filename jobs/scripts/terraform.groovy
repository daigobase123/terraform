// 変数を定義
def apply = "0"
def planExitCode
def applyExitCode

pipeline {
    agent any
    // 環境変数を定義
    environment {
        gf_base_path="${workspace}/gf/aws-lambda"
        TERRAFORM_HOME = "${gf_base_path}/jobs/terraform"
    }

    // ansiエスケープシーケンスでログに色をつける
    options {
        ansiColor('xterm')
    }

    stages {
        stage('Initialize') {
            steps {
                // terraformコードがあるディレクトリで処理
                dir(TERRAFORM_HOME) {
                    script {
                        // terraformのバージョン確認
                        sh "terraform -v"
                        // 前回のジョブ実行時のファイルを削除
                        if (fileExists(".terraform/terraform.tfstate")) {
                            sh "rm -rf .terraform/terraform.tfstate"
                        }
                        if (fileExists(".terraform.lock.hcl")) {
                            sh "rm -rf .terraform.lock.hcl"
                        }
                        if (fileExists("status")) {
                            sh "rm status"
                        }
                        unzip terraform.zip
                        // terraform initの実行
                        sh "terraform init"
                    }
                }
            }
        }

        stage('plan') {
            steps {
                // terraformコードがあるディレクトリで処理
                dir(TERRAFORM_HOME) {
                    script {
                        // terraform planの実行
                        sh "set +e; terraform plan -out=plan.out -detailed-exitcode; echo \$? > status"
                        planExitCode = readFile('status').trim()
                        println "Terraform Plan Exit Code: ${planExitCode}"
                        // plan成功時かつ差分がない場合
                        if (planExitCode == "0") {
                            currentBuild.result = 'SUCCESS'
                            apply = "0"
                        }
                        // plan失敗時
                        if (planExitCode == "1") {
                            currentBuild.result = 'FAILURE'
                            apply = "0"
                        }
                        // plan成功時かつ差分がある場合
                        if (planExitCode == "2") {
                            stash name: "plan", includes: "plan.out"
                            try {
                                // 承認フェーズ
                                if (apply != "1") {
                                    input message: 'Apply Plan?', ok: 'Apply'
                                }
                                apply = "1"
                            } catch (err) {
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                }
            }
        }

        stage('Apply') {
            // apply変数が1の場合、apply実行
            when {
                expression { apply == "1" }
            }
            steps {
                // terraformコードがあるディレクトリで処理
                dir(TERRAFORM_HOME) {
                    script {
                        unstash 'plan'
                        // 前回のジョブ実行時のファイルを削除
                        if (fileExists("status.apply")) {
                            sh "rm status.apply"
                        }
                        // terraform applyの実行
                        ansiColor('xterm') {
                            sh "set +e; terraform apply plan.out; echo \$? > status.apply"
                        }
                        applyExitCode = readFile('status.apply').trim()
                        println "applyExit Code: " + applyExitCode
                        // apply成功時
                        if (applyExitCode == "0") {
                            currentBuild.result = 'SUCCESS'
                        } // apply失敗時
                        else {
                            currentBuild.result = 'FAILURE'
                        }
                        println "currentBuild.result :" + currentBuild.result
                    }
                }
            }
        }
    }
}