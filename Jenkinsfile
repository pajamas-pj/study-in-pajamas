pipeline {
    agent any

    tools {
        maven 'Maven 3.9.10'
        jdk 'JDK 11'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        APP_NAME = 'pj-gateway'
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('checkout') {
            steps {
                echo '拉取最新代码...'
                // 直接使用内置 git，代码会拉到当前工作空间根目录
                git url: 'git@github.com:pajamas-pj/study-in-pajamas.git', branch: 'master'
            }
        }

        stage('build pj-gateway') {
            steps {
                echo '开始构建 pj-gateway 模块...'
                sh 'mvn clean package -pl pj-gateway -am -DskipTests'
            }
            post {
                success {
                    echo '✅ pj-gateway 模块打包成功！'
                    // 【修复1】必须用双引号，否则 ${APP_NAME} 不生效
                    archiveArtifacts artifacts: "${APP_NAME}/target/*.jar", allowEmptyArchive: true
                }
                failure {
                    echo '❌ pj-gateway 模块打包失败，请查日志！'
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def remoteHost = 'root@118.178.172.169'
                    def deployPath = '/opt/developments'
                    def jarFile = "${APP_NAME}.jar"

                    sh """
                        echo '正在上传 ${jarFile}...'

                        # 1. 确保远程目录存在
                        ssh ${remoteHost} "mkdir -p ${deployPath}"

                        # 2. 上传 JAR 包到宿主机
                        scp -O ${APP_NAME}/target/${jarFile} ${remoteHost}:${deployPath}/

                        echo '正在远程通过 Docker 部署...'

                        # 3. 远程执行 Docker 替换命令（Heredoc 语法）
                        ssh -T ${remoteHost} << 'ENDSSH'
set -e
DEPLOY_PATH="${deployPath}"
JAR_FILE="${jarFile}"

echo "进入目录 \$DEPLOY_PATH"
cd \$DEPLOY_PATH

echo "停止并删除旧容器..."
# 忽略报错，防止第一次部署时找不到容器导致流水线中断
docker stop pj-gateway || true
docker rm pj-gateway || true

echo "启动新的 Docker 容器..."
docker run -d \
  --name pj-gateway \
  --restart always \
  --network pajamas \
  -p 10010:10010 \
  -v \$DEPLOY_PATH:/app \
  openjdk:11-jre-slim \
  java -Xms256m -Xmx512m -jar /app/\$JAR_FILE

echo "部署流程执行完毕！"
ENDSSH
                    """
                }
            }
        }
    } // 闭合 stages
} // 闭合 pipeline