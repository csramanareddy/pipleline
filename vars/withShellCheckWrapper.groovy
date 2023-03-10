#!/usr/bin/groovy
import hudson.model.*

def call(Closure body=null) {
  this.vars = [:]
  call(vars, body)
}

def call(Map vars, Closure body=null) {
  echo '[JPL] Executing `vars/withShellCheckWrapper.groovy`'

  vars = vars ?: [:]

  def SHELLCHECK_OPTS = vars.get('SHELLCHECK_OPTS', env.SHELLCHECK_OPTS ?: '-e SC2154 -e SC2086').trim()

  vars.pattern = vars.get('pattern', '*.sh')
  vars.shellCheckCmdParameters = vars.get('shellCheckCmdParameters', '')
  vars.shellOutputFile = vars.get('shellOutputFile', 'shellCheck.log').trim()
  vars.shellCheckFileId = vars.get('shellCheckFileId', '0').trim()

  vars.skipShellCheckFailure = vars.get('skipShellCheckFailure', true).toBoolean()
  vars.skipShellCheck = vars.get('skipShellCheck', true).toBoolean()
  vars.shellCheckOutputFile = vars.get('shellCheckOutputFile', "shellcheck-${vars.shellCheckFileId}.json").trim()

  vars.isSuccessReturnCode = vars.get('isSuccessReturnCode', 0)
  vars.isFailReturnCode = vars.get('isFailReturnCode', 255)
  vars.isUnstableReturnCode = vars.get('isUnstableReturnCode', 1)

  vars.isFingerprintEnabled = vars.get('isFingerprintEnabled', false).toBoolean()

  if (!vars.skipShellCheck) {
    tee("${vars.shellOutputFile}") {
      if (isUnix()) {
        shellCheckExitCode = sh(
					script: """#!/bin/bash -l
					export SHELLCHECK_OPTS=\"${SHELLCHECK_OPTS}\"
					shellcheck ${vars.shellCheckCmdParameters} -f json ${vars.pattern} 2>&1 > ${vars.shellCheckOutputFile}""",
					returnStdout: true,
					returnStatus: true
				)

        echo "SHELLCHECK RETURN CODE : ${shellCheckExitCode}"
        if (shellCheckExitCode == vars.isSuccessReturnCode) {
          echo 'SHELLCHECK SUCCESS'
        } else if (!vars.skipShellCheckFailure) {
          if (shellCheckExitCode == vars.isFailReturnCode) {
            echo 'SHELLCHECK FAILURE'
            currentBuild.result = 'FAILURE'
            error 'There are errors in shellCheck'
          } else if (shellCheckExitCode <= vars.isUnstableReturnCode) {
            echo 'SHELLCHECK UNSTABLE'
            currentBuild.result = 'UNSTABLE'
          } else {
            echo 'SHELLCHECK FAILURE'
            //currentBuild.result = 'FAILURE'
            error 'There are other errors'
          }
        } else {
          echo 'SHELLCHECK FAILURE skipped'
        //error 'There are errors in shellCheck'
        }
			} // isUnix
		} // tee

    // deprecated plugin https://plugins.jenkins.io/checkstyle/
		//checkstyle canComputeNew: false, defaultEncoding: '', healthy: '50', pattern: "${vars.shellCheckOutputFile}", shouldDetectModules: true, thresholdLimit: 'normal', unHealthy: '100'
	  archiveArtifacts artifacts: "${vars.shellCheckOutputFile}", excludes: null, fingerprint: vars.isFingerprintEnabled, onlyIfSuccessful: false, allowEmptyArchive: true

		return shellCheckExitCode
	} else {
    echo 'ShellCheck skipped'
  }
}
