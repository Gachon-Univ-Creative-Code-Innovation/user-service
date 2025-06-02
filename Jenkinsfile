pipeline {
  agent any

  environment {
    DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
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

    stage('Build Docker Image') {
      steps {
        script {
          sh "docker build -t ${IMAGE_NAME}:${TAG} ."
        }
      }
    }

    stage('Push to DockerHub') {
      steps {
        script {
          sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
          sh "docker push ${IMAGE_NAME}:${TAG}"
        }
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        script {
          // 이미지 PullSecret이 설정된 deployment YAML을 사용
          sh "kubectl apply -f ${DEPLOYMENT_FILE} -n ${NAMESPACE}"
          sh "kubectl rollout restart deployment user-service -n ${NAMESPACE}"
        }
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