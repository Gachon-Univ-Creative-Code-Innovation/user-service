podTemplate(yaml: '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command:
    - cat              # 컨테이너가 바로 종료되지 않고 대기하도록 설정
    tty: true
    volumeMounts:
    - name: kaniko-secret
      mountPath: /kaniko/.docker   # Kaniko가 Docker 레지스트리 인증에 사용할 위치
  - name: jnlp
    image: msj9965/jenkins-agent:java17
    volumeMounts:
    - name: workspace-volume
      mountPath: /home/jenkins/agent
  volumes:
  - name: kaniko-secret
    secret:
      secretName: dockercred       # 반드시 Kaniko 인증 시크릿 이름과 맞게 설정
  - name: workspace-volume
    emptyDir: {}
''') {
  node(POD_LABEL) {
    stage('Checkout') {
      checkout([$class: 'GitSCM', branches: [[name: 'main']],
                userRemoteConfigs: [[url: 'https://github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git']]])
    }

    stage('Build Jar') {
      sh 'chmod +x gradlew'
      sh './gradlew build -x test'
    }

    stage('Build and Push Docker Image with Kaniko') {
      container('kaniko') {
        // 빌드 결과물이 잘 있는지 확인하는 단계 (디버깅용)
        sh 'ls -al /home/jenkins/agent'
        sh 'ls -al /home/jenkins/agent/build/libs'   // gradle jar 위치 예시

        // Kaniko 빌드 및 푸시 명령어
        sh '''
          /kaniko/executor \
          --dockerfile=/home/jenkins/agent/Dockerfile \
          --context=/home/jenkins/agent \
          --destination=msj9965/alog-user:latest \
          --verbosity=info
        '''
      }
    }

    stage('Deploy to Kubernetes') {
      sh 'kubectl apply -f user-service-deployment.yaml -n user'
      sh 'kubectl rollout restart deployment user-service -n user'
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
}