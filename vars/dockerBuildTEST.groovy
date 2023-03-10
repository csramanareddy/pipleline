#!/usr/bin/groovy
import hudson.model.*

def call(Closure body=null) {
  this.vars = [:]
  call(vars, body)
}

def call(Map vars, Closure body=null) {
  vars = vars ?: [:]

  def CLEAN_RUN = vars.get('CLEAN_RUN', env.CLEAN_RUN ?: true).toBoolean()
  def DRY_RUN = vars.get('DRY_RUN', env.DRY_RUN ?: false).toBoolean()
    //def DEBUG_RUN = vars.get("DEBUG_RUN", env.DEBUG_RUN ?: false).toBoolean()
    //def RELEASE_VERSION = vars.get("RELEASE_VERSION", env.RELEASE_VERSION ?: null)
    //def RELEASE = vars.get("RELEASE", env.RELEASE ?: false).toBoolean()
    //def RELEASE_BASE = vars.get("RELEASE_BASE", env.RELEASE_BASE ?: null)

    //String DOCKER_REGISTRY = vars.get("DOCKER_REGISTRY", env.DOCKER_REGISTRY ?: "registry.hub.docker.com").toLowerCase().trim()
    //String DOCKER_REGISTRY_URL = vars.get("DOCKER_REGISTRY_URL", env.DOCKER_REGISTRY_URL ?: "https://${DOCKER_REGISTRY}")
    //String DOCKER_REGISTRY_CREDENTIAL = vars.get("DOCKER_REGISTRY_CREDENTIAL", env.DOCKER_REGISTRY_CREDENTIAL ?: "hub-docker-nabla")

  String DOCKER_ORGANISATION = vars.get('DOCKER_ORGANISATION', env.DOCKER_ORGANISATION ?: 'nabla')

  String DOCKER_BUILD_TAG = vars.get('DOCKER_BUILD_TAG', env.DOCKER_BUILD_TAG ?: 'latest')
  //if (env.BRANCH_NAME ==~ /release|master|develop/ ) {
  //    DOCKER_BUILD_TAG="${env.BUILD_ID}"
  //}
  String DOCKER_BUILD_NAME = vars.get('DOCKER_BUILD_NAME', env.DOCKER_BUILD_NAME ?: 'jenkins-slave-test-centos7')
  String DOCKER_BUILD_IMG = vars.get('DOCKER_BUILD_IMG', env.DOCKER_BUILD_IMG ?: "${DOCKER_ORGANISATION}/${DOCKER_BUILD_NAME}:${DOCKER_BUILD_TAG}")

  vars.isScmEnabled = vars.get('isScmEnabled', false).toBoolean()
  def dockerFilePath = vars.get('dockerFilePath', env.dockerFilePath ?: './')

  script {
        wrapInTEST(isScmEnabled: vars.isScmEnabled) {
      //sh(returnStdout: true, script: "echo ${DOCKER_BUILD_IMG} | cut -d'/' -f -1").trim()
      DOCKER_BUILD_ARGS = [''].join(' ')
      if (CLEAN_RUN) {
        DOCKER_BUILD_ARGS = ['--no-cache',
                                     '--pull',
                                     ].join(' ')
      }

      // TODO withRegistry is buggy, because of wrong DOCKER_CONFIG
      //docker.withRegistry(DOCKER_REGISTRY_HUB_URL, DOCKER_REGISTRY_HUB_CREDENTIAL) {
      def container = docker.build("${DOCKER_BUILD_IMG}", "${DOCKER_BUILD_ARGS} -f ${dockerFilePath}Dockerfile . ")
      container.inside {
        sh 'echo test'
        if (body) { body() }
      }
      if (!DRY_RUN && isReleaseBranch() ) {
        echo 'TODO : Push the container to the custom Registry'
      //image.push("${env.BUILD_NUMBER}")
      //image.push('latest')
      }
            //} // withRegistry

        //dockerFingerprintFrom dockerfile: "${dockerFilePath}Dockerfile", image: "${DOCKER_BUILD_IMG}"
        }  // wrapInTEST
    } // script

  return DOCKER_BUILD_TAG
}
