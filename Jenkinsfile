pipeline {
  agent {
    label 'kubeagent'
  }

  environment {
    JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
    PATH = "/usr/lib/jvm/java-17-openjdk-amd64/bin:$PATH"
    IMAGE_NAME = 'msj9965/alog-user'
    TAG = "latest"
    DEPLOYMENT_FILE = 'user-service-deployment.yaml'
    NAMESPACE = 'user'
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git'
      }
    }

    stage('Build Jar') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew build -x test'
      }
    }

    stage('Build and Push Docker Image with Kaniko') {
          steps {
            container('kaniko') {
              // 1. 가장 기본적인 쉘 명령 실행 테스트 (BusyBox 쉘 명시)
              sh '''#!/busybox/sh
    echo "Attempting to run commands inside Kaniko container with busybox sh..."
    pwd
    ls -la
    printenv
    echo "Basic commands finished."
    '''
              // 2. 원래 Kaniko 실행 명령 (마찬가지로 BusyBox 쉘 명시)
              sh '''#!/busybox/sh -xe
    echo "Kaniko container command start"
    /kaniko/executor \
    --dockerfile=Dockerfile \
    --context=$(pwd) \
    --destination=$IMAGE_NAME:$TAG \
    --verbosity=info
    '''
            }
          }
        }

    stage('Deploy to Kubernetes') {
      steps {
        sh "kubectl apply -f ${DEPLOYMENT_FILE} -n ${NAMESPACE}"
        sh "kubectl rollout restart deployment user-service -n ${NAMESPACE}"
      }
    }
  }

  post {
    success {
      echo '✅ 배포 성공!'
    }
    failure {
      echo '❌ 배포 실패. 로그를 확인해주세요.'
    }
  }
}