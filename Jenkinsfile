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
              def kanikoPodName = "${KANIKO_POD_NAME_PREFIX}-${BUILD_NUMBER}-debug".toLowerCase() // 디버깅용 Pod 이름
              // kanikoArgs는 이 디버깅 단계에서는 사용되지 않습니다.

              def podManifest = """
              apiVersion: v1
              kind: Pod
              metadata:
                name: ${kanikoPodName}
                namespace: ${NAMESPACE}
                labels:
                  jenkins-build: "${BUILD_NUMBER}"
                  app: kaniko-debug
              spec:
                containers:
                - name: kaniko-debug-container # 컨테이너 이름도 변경 가능
                  image: '${KANIKO_EXECUTOR_IMAGE}' # Kaniko 이미지를 사용 (busybox 쉘 포함)
                  command: ["/busybox/sh", "-c"]
                  args:
                    - > # 여러 명령을 깔끔하게 실행하기 위해 YAML의 >- (fold) 스타일 사용
                      echo "========= Kaniko Pod Internal Debug Start =========";
                      echo "--- Current User & Group ---";
                      id;
                      echo "\\n--- Listing /kaniko/ ---";
                      ls -la /kaniko/;
                      echo "\\n--- Listing /kaniko/.docker/ ---";
                      ls -la /kaniko/.docker/;
                      echo "\\n--- Content of /kaniko/.docker/config.json ---";
                      cat /kaniko/.docker/config.json || echo "Error: /kaniko/.docker/config.json not found or cannot be read";
                      echo "\\n--- Environment Variables (DOCKER_CONFIG check) ---";
                      printenv | grep DOCKER_CONFIG || echo "DOCKER_CONFIG environment variable not set";
                      echo "\\n========= Kaniko Pod Internal Debug End =========";
                      echo "Debug Pod will sleep for 5 minutes for manual inspection if needed, then exit.";
                      sleep 300;
                      echo "Exiting debug pod."
                  volumeMounts:
                    - name: docker-config
                      mountPath: /kaniko/.docker # Kaniko가 config.json을 찾는 기본 경로
                      readOnly: true
                restartPolicy: Never # 디버깅 후에는 Pod가 재시작되지 않도록 Never 유지
                volumes:
                  - name: docker-config
                    secret:
                      secretName: "dockercred"
              """

              try {
                echo "Applying Kaniko DEBUG Pod manifest for: ${kanikoPodName}"
                sh "echo '''${podManifest}''' | kubectl apply -f -"

                echo "Waiting for Kaniko DEBUG Pod ${kanikoPodName} to be Ready..."
                sh "kubectl wait --for=condition=Ready pod/${kanikoPodName} -n ${NAMESPACE} --timeout=3m"

                echo "Kaniko DEBUG pod ${kanikoPodName} is Ready. Fetching logs (will show config.json content, etc.):"
                // Pod가 완료될 때까지 기다리지 않고, 실행 중인 로그를 가져옵니다.
                // sleep 명령어 때문에 Pod는 'Succeeded' 상태가 되지 않을 수 있습니다.
                // Jenkins는 이 sh 명령이 완료될 때까지 기다립니다 (sleep 시간만큼).
                // 더 나은 방법은 Jenkins 로그에서 직접 확인하거나, 별도 터미널에서 kubectl logs를 사용하는 것입니다.
                sh "kubectl logs pod/${kanikoPodName} -n ${NAMESPACE} --timestamps"

                echo "Debug script in Kaniko pod has finished (or timed out waiting for logs)."
                // 이 디버깅 실행은 '성공'으로 간주하지 않고, 정보 수집이 목적입니다.
                // 따라서 currentBuild.result를 FAILURE로 설정하거나 에러를 발생시키지 않습니다.

              } catch (Exception e) {
                echo "Error during Kaniko DEBUG Pod setup or log retrieval for pod ${kanikoPodName}: ${e.message}"
                // 실패 시에도 로그를 가져오도록 시도
                sh "kubectl logs pod/${kanikoPodName} -n ${NAMESPACE} --timestamps || true"
                // 디버깅 목적이므로, 파이프라인을 실패시키지 않을 수 있습니다.
                // 하지만 어떤 오류인지 파악하기 위해 일단은 실패로 처리합니다.
                currentBuild.result = 'FAILURE'
                error("Kaniko DEBUG pod execution encountered an error.")
              } finally {
                echo "Deleting Kaniko DEBUG pod ${kanikoPodName}..."
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