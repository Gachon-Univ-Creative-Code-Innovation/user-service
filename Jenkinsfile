podTemplate(
    label: 'kubeagent',
    containers: [
        containerTemplate(
            name: 'jnlp',
            image: 'jenkins/inbound-agent:latest-jdk17',
            ttyEnabled: true,
            command: ''
        )
    ]
) {
    node('kubeagent') {
        stage('환경 변수 설정') {
            container('jnlp') {
                env.JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"
                env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
                env.IMAGE_NAME = 'msj9965/alog-user'
                env.TAG = "latest"
                env.DEPLOYMENT_FILE = 'user-service-deployment.yaml'
                env.NAMESPACE = 'user'
            }
        }

        stage('Checkout') {
            container('jnlp') {
                git branch: 'main', url: 'https://github.com/Gachon-Univ-Creative-Code-Innovation/user-service.git'
            }
        }

        stage('Build Jar') {
            container('jnlp') {
                sh 'chmod +x gradlew'
                sh './gradlew build -x test'
            }
        }

        stage('Build Docker Image') {
            container('jnlp') {
                sh "docker build -t ${env.IMAGE_NAME}:${env.TAG} ."
            }
        }

        stage('Push to DockerHub') {
            container('jnlp') {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh "echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin"
                    sh "docker push ${env.IMAGE_NAME}:${env.TAG}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            container('jnlp') {
                sh "kubectl apply -f ${env.DEPLOYMENT_FILE} -n ${env.NAMESPACE}"
                sh "kubectl rollout restart deployment user-service -n ${env.NAMESPACE}"
            }
        }

        stage('Finish') {
            echo '✅ 배포 성공!'
        }
    }
}