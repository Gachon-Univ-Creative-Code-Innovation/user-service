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
          sh '''
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