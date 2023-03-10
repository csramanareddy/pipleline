#!/usr/bin/groovy
import static com.test.jenkins.sonar.Sonar.getSonarInclusions

import hudson.model.*

def call(Closure body=null) {
  this.vars = [:]
  call(vars, body)
}

def call(Map vars, Closure body=null) {
  echo '[JPL] Executing `vars/withSonarQubeWrapper.groovy`'

  vars = vars ?: [:]

  getJenkinsOpts(vars)

  vars.propertiesPath = vars.get('propertiesPath', 'sonar-project.properties')
  vars.bwoutputs = vars.get('bwoutputs', '').trim()
  vars.coverage = vars.get('coverage', '').trim()
  vars.verbose = vars.get('verbose', false).toBoolean()
  vars.buildCmdParameters = vars.get('buildCmdParameters', '').trim()
  vars.project = vars.get('project', 'NABLA').trim()
  //vars.projectVersion = vars.get("projectVersion", "")
  //vars.repository = vars.get("repository", "").trim()
  vars.skipMaven = vars.get('skipMaven', true).toBoolean()
  vars.skipFailure = vars.get('skipFailure', true).toBoolean()
  vars.skipInclusion = vars.get('skipInclusion', false).toBoolean()
  vars.skipSonarCheck = vars.get('skipSonarCheck', true).toBoolean()
  vars.targetBranch = vars.get('targetBranch', 'develop').trim()
  vars.isScannerHome = vars.get('isScannerHome', true).toBoolean()
  if (vars.isScannerHome == true) {
    def scannerHome = tool name: "${vars.SONAR_SCANNER}", type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    vars.sonarExecutable = vars.get('sonarExecutable', "${scannerHome}/bin/sonar-scanner")
    } else {
    // docker
    vars.sonarExecutable = vars.get('sonarExecutable', '/usr/local/sonar-runner/bin/sonar-scanner')
  }
  vars.isFingerprintEnabled = vars.get('isFingerprintEnabled', false).toBoolean()
  vars.shellOutputFile = vars.get('shellOutputFile', 'sonar.log').trim()

  script {
        if (!vars.DRY_RUN && !vars.RELEASE) {
      tee("${vars.shellOutputFile}") {
        if (!vars.skipMaven) {
          if (vars.coverage?.trim()) {
            unstash vars.coverage
          }
          unstash 'maven-artifacts'
          unstash 'classes'
        }

        if (vars.bwoutputs?.trim()) {
          unstash vars.bwoutputs
        }

        if (vars.DEBUG_RUN) {
          echo "SONAR_INSTANCE: ${vars.SONAR_INSTANCE}"
          vars.verbose = true
        }

        vars.buildCmdParameters += ' -Dproject.settings=' + vars.propertiesPath

        if (!vars.RELEASE_VERSION) {
          echo 'No RELEASE_VERSION specified'
          vars.RELEASE_VERSION = getSemVerReleasedVersion(vars) ?: '0.0.1'
          vars.projectVersion = "${vars.RELEASE_VERSION}"
        }

        if (vars.projectVersion?.trim()) {
          vars.buildCmdParameters += " -Dsonar.projectVersion=${vars.projectVersion} "
        }

        if (vars.verbose) {
          vars.buildCmdParameters += ' -X -Dsonar.verbose=true '
        }

        if ( env.BRANCH_NAME ==~ /master|master_.+|release\/.+/ ) {
          echo '[JPL] isReleaseBranch, so no check for `Sonar.getSonarInclusions`'
                } else {
          if (!vars.skipInclusion) {
            vars.jobName = vars.get('jobName', env.JOB_NAME)
            vars.currentRevision = vars.get('currentRevision', env.GIT_COMMIT)

            try {
              // SynchronousNonBlockingStepExecution with usernamePassword not available in static groovy JPL
              withCredentials([
                                usernamePassword(
                                credentialsId: vars.STASH_CREDENTIALS,
                                usernameVariable: 'stashLogin',
                                passwordVariable: 'stashPass'
                                )
                            ]) {
                vars.basicAuth = "${stashLogin}:${stashPass}".getBytes().encodeBase64().toString()
                            }

              println("[JPL] Full CONFIG after applying the default values for getSonarInclusions is: ${vars}")
              vars.buildCmdParameters += getSonarInclusions(vars)
            }
                        catch (exc) {
              echo 'Error: There were errors to retrieve credentials. ' + exc // but we do not fail the whole build because of that
                        }
          }
        }

                // TODO Remove it when tee will be back
                //vars.buildCmdParameters += " 2>&1 > ${vars.shellOutputFile} "

        echo "Sonar GOALS have been specified: ${vars.buildCmdParameters}"

        withSonarQubeEnv("${vars.SONAR_INSTANCE}") {
          if (body) {
            body()
          }
          def build = 'FAIL'

          echo "${vars.sonarExecutable} -Dsonar.branch.name=${env.BRANCH_NAME} " + vars.buildCmdParameters + ' '

          if ( BRANCH_NAME ==~ /master|master_.+/ ) {
            build = sh (
                                // The main branch must not have a target
                                script: "${vars.sonarExecutable} -Dsonar.branch.name=${env.BRANCH_NAME} " + vars.buildCmdParameters + ' ',
                                returnStatus: true
                                )
                    } else if ( BRANCH_NAME ==~ /develop/ ) {
            build = sh (
                                script: "${vars.sonarExecutable} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop " + vars.buildCmdParameters + ' ',
                                returnStatus: true
                                )
                    } else if ( BRANCH_NAME ==~ /release\/.+/ ) {
            build = sh (
                                script: "${vars.sonarExecutable} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=master " + vars.buildCmdParameters + ' ',
                                returnStatus: true
                                )
                    } else {
            build = sh (
                                script: "${vars.sonarExecutable} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=${vars.targetBranch} " + vars.buildCmdParameters + ' ',
                                returnStatus: true
                                )
          }

          echo "SONAR RETURN CODE : ${build}"
          if (build == 0) {
            echo 'SONAR SUCCESS'
                    } else {
            echo 'SONAR UNSTABLE'
            echo "WARNING : Sonar scan failed, check output at \'${vars.shellOutputFile}\' "
            if (!vars.skipFailure) {
              currentBuild.result = 'UNSTABLE'
              echo 'WARNING : There are errors in sonar'
            }
          }

          if (!vars.skipSonarCheck) {
            withSonarQubeCheck(vars)
          }
                } // withSonarQubeEnv

        archiveArtifacts artifacts: "${vars.shellOutputFile}, **/report-task.txt", excludes: null, fingerprint: vars.isFingerprintEnabled, onlyIfSuccessful: false, allowEmptyArchive: true
            } // tee
        } // if DRY_RUN
    } // script
}
