pipeline {
  agent {
    label 'kubeagent'
  }

  environment {
      JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
      PATH = "/usr/lib/jvm/java-17-openjdk-amd64/bin:$PATH"
      IMAGE_NAME = 'msj9965/alog-user'
      TAG = "latest" // 또는 동적으로 생성하려면 Checkout 단계에서 GIT_COMMIT_SHA를 얻어 설정
      DEPLOYMENT_FILE = 'user-service-deployment.yaml'
      NAMESPACE = 'user'

      // Kaniko Pod 실행용 환경 변수
      KANIKO_EXECUTOR_IMAGE = 'gcr.io/kaniko-project/executor:v1.12.1'
      GIT_REPO_URL = 'https://github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git'
      KANIKO_POD_NAME_PREFIX = "kaniko-user-service"
      DOCKERFILE_PATH = 'Dockerfile'
      // Kaniko가 항상 main 브랜치의 최신 코드를 사용하도록 여기서 설정
      KANIKO_GIT_CONTEXT = "${GIT_REPO_URL}#refs/heads/main"
  }

  stages {
    stage('Checkout') {
      steps {
        // Jenkins 에이전트 작업 공간으로 소스 코드 체크아웃
        // 이 단계는 'Build Jar' 단계에서 사용할 코드를 가져옵니다.
        // Jenkinsfile이 SCM에서 로드될 때 이미 checkout이 수행되므로,
        // 이 단계가 반드시 명시적으로 필요하지 않을 수도 있지만,
        // 파이프라인의 명확성을 위해 두는 것이 일반적입니다.
        // 간단히 checkout scm 또는 특정 브랜치를 지정할 수 있습니다.
        git branch: 'main', url: GIT_REPO_URL // 또는 checkout scm
        script {
          // 이미지 태그를 Git 커밋 기반으로 동적으로 설정하고 싶다면, 여기서 작업:
          // def commit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
          // def shortCommit = commit.substring(0,7)
          // env.TAG = "git-${shortCommit}"
          // echo "Image tag set to: ${env.TAG}"

          // 만약 KANIKO_GIT_CONTEXT를 현재 체크아웃된 커밋으로 하고 싶다면 여기서 설정:
          // def currentCommit = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
          // env.KANIKO_GIT_CONTEXT = "${GIT_REPO_URL}#${currentCommit}"
          // echo "Kaniko Git context updated to current commit: ${env.KANIKO_GIT_CONTEXT}"
          // (이 경우 environment 블록의 KANIKO_GIT_CONTEXT는 초기값으로만 사용되거나 제거)
        }
      }
    }

    stage('Build Jar') { // 이 단계는 Checkout된 소스를 사용
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew build -x test'
      }
    }

    stage('Build and Push Docker Image with Kaniko Pod') {
      steps {
        script {
          def kanikoPodName = "${KANIKO_POD_NAME_PREFIX}-${BUILD_NUMBER}".toLowerCase()
          def podManifest = """
          apiVersion: v1
          kind: Pod
          metadata:
            name: ${kanikoPodName}
            namespace: ${NAMESPACE}
            labels:
              jenkins-build: "${BUILD_NUMBER}"
              app: kaniko
          spec:
            containers:
            - name: kaniko
              image: '${KANIKO_EXECUTOR_IMAGE}'
              args:
                - "--dockerfile=${DOCKERFILE_PATH}"
                - "--context=${env.KANIKO_GIT_CONTEXT}" // environment 블록 또는 Checkout에서 최종 설정된 값 사용
                - "--destination=${IMAGE_NAME}:${TAG}"
                - "--verbosity=info"
              volumeMounts:
                - name: docker-config
                  mountPath: /kaniko/.docker
                  readOnly: true
            restartPolicy: Never
            volumes:
              - name: docker-config
                secret:
                  secretName: "dockercred"
          """
          try {
            echo "Applying Kaniko Pod manifest for: ${kanikoPodName} with context ${env.KANIKO_GIT_CONTEXT}"
            sh "echo '${podManifest}' | kubectl apply -f -"
            sh "kubectl wait --for=condition=Ready pod/${kanikoPodName} -n ${NAMESPACE} --timeout=5m"
            echo "Kaniko pod ${kanikoPodName} is Ready. Waiting for build completion..."
            sh "kubectl wait --for=condition=Succeeded pod/${kanikoPodName} -n ${NAMESPACE} --timeout=20m"
            echo "Kaniko build successful for pod: ${kanikoPodName}"
            echo "Fetching Kaniko pod logs:"
            sh "kubectl logs pod/${kanikoPodName} -n ${NAMESPACE}"
          } catch (Exception e) {
            echo "Error during Kaniko Pod execution for pod ${kanikoPodName}: ${e.message}"
            echo "Attempting to fetch logs from Kaniko pod (on error):"
            sh "kubectl logs pod/${kanikoPodName} -n ${NAMESPACE} || true"
            currentBuild.result = 'FAILURE'
            error("Kaniko build failed. See logs above for details.")
          } finally {
            echo "Deleting Kaniko pod ${kanikoPodName}..."
            sh "kubectl delete pod/${kanikoPodName} -n ${NAMESPACE} --ignore-not-found=true"
          }
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