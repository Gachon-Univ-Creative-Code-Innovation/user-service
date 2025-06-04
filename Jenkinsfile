pipeline {
  agent {
    label 'kubeagent'
  }

  environment {
      // ...
      IMAGE_NAME = 'msj9965/alog-user'
      TAG = "latest"
      DEPLOYMENT_FILE = 'user-service-deployment.yaml'
      NAMESPACE = 'user'

      KANIKO_EXECUTOR_IMAGE = 'gcr.io/kaniko-project/executor:v1.12.1'
      // HTTPS URL (git checkout용)
      HTTPS_GIT_REPO_URL = 'https://github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git'
      // 프로토콜 없는 URL (Kaniko git:// 스킴 조합용)
      BARE_GIT_REPO_URL = 'github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git'
      KANIKO_POD_NAME_PREFIX = "kaniko-user-service"
      DOCKERFILE_PATH = 'Dockerfile'
      // Kaniko가 항상 main 브랜치의 최신 코드를 사용하도록 여기서 설정
      KANIKO_GIT_CONTEXT = "git://${BARE_GIT_REPO_URL}#refs/heads/main"
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: HTTPS_GIT_REPO_URL // HTTPS_GIT_REPO_URL 사용
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
              // args 리스트를 Groovy 리스트로 먼저 정의
              def kanikoArgs = [
                "--dockerfile=${DOCKERFILE_PATH}",
                "--context=${env.KANIKO_GIT_CONTEXT}",
                "--destination=${IMAGE_NAME}:${TAG}",
                "--verbosity=info"
                // 필요시 Kaniko 캐시 설정 추가
                // "--cache=true",
                // "--cache-repo=${IMAGE_NAME}-cache"
              ]

              // YAML 문자열을 생성할 때 args 부분을 명시적으로 배열로 변환하여 주입
              // 또는, 아래와 같이 직접 YAML 배열 형식으로 멀티라인 문자열에 포함
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
                    - "${kanikoArgs[0]}"
                    - "${kanikoArgs[1]}"
                    - "${kanikoArgs[2]}"
                    - "${kanikoArgs[3]}"
                    # 만약 캐시 인수가 있다면 추가:
                    # - "${kanikoArgs[4]}"
                    # - "${kanikoArgs[5]}"
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

              // 디버깅을 위해 최종 YAML 출력 (선택 사항)
              // echo "Generated Kaniko Pod Manifest:\n${podManifest}"

              try {
                echo "Applying Kaniko Pod manifest for: ${kanikoPodName} with context ${env.KANIKO_GIT_CONTEXT}"
                sh "echo '''${podManifest}''' | kubectl apply -f -" // 삼중 작은따옴표로 변경 시도
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