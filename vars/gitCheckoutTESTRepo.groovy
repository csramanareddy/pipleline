#!/usr/bin/groovy

// TODO
def gitClone(repoUrl, relativeTargetDir) {
  git_cmd = sh (
        script: "git checkout ${repoUrl} ${relativeTargetDir}",
        returnStdout: true
    ).trim()
  return git_cmd
}

def call(Closure body=null) {
  this.vars = [:]
  call(vars, body)
}

def call(Map vars, Closure body=null) {
  echo '[JPL] Executing `vars/gitCheckoutTESTRepo.groovy`'

  vars = vars ?: [:]

  vars.GIT_REPO_PROJECT = vars.get('GIT_PROJECT_TEST', 'NABLA').trim()
  vars.GIT_PROJECT_TEST = vars.get('GIT_PROJECT_TEST', 'nabla-servers-bower-sample').trim()
  vars.GIT_BROWSE_URL_TEST = vars.get('GIT_BROWSE_URL_TEST', "https://github.com/AlbanAndrieu/${vars.GIT_PROJECT_TEST}/").trim()
  vars.GIT_URL_TEST = vars.get('GIT_URL_TEST', "https://github.com/AlbanAndrieu/${vars.GIT_PROJECT_TEST}.git").trim()
  vars.JENKINS_CREDENTIALS = vars.get('JENKINS_CREDENTIALS', env.JENKINS_SSH_CREDENTIALS ?: 'stash-jenkins').trim()
    //vars.GIT_URL_TEST = vars.get("GIT_URL_TEST", "ssh://git@github.com:AlbanAndrieu/${vars.GIT_REPO_PROJECT}/${vars.GIT_PROJECT}.git").trim()
    //vars.JENKINS_CREDENTIALS = vars.get("JENKINS_CREDENTIALS", "jenkins-ssh")

  vars.isScmEnabled = vars.get('isScmEnabled', true).toBoolean()
  vars.isDefaultBranch = vars.get('isDefaultBranch', false).toBoolean()
  vars.relativeTargetDir = vars.get('relativeTargetDir', vars.GIT_PROJECT_TEST).trim()
  vars.timeout = vars.get('timeout', 20)
  vars.isCleaningEnabled = vars.get('isCleaningEnabled', true).toBoolean()
  vars.isShallowEnabled = vars.get('isShallowEnabled', true).toBoolean()

  vars.GIT_BRANCH_NAME = vars.get('GIT_BRANCH_NAME', 'develop')

  if (vars.isScmEnabled) {
    checkout([
           $class: 'GitSCM',
           branches: getDefaultCheckoutBranches(vars),
           browser: [
               $class: 'Stash',
               repoUrl: "${vars.GIT_BROWSE_URL_TEST}"],
           doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
           extensions: getDefaultCheckoutExtensions(vars),
           gitTool: 'git-latest',
           submoduleCfg: [],
           userRemoteConfigs: [[
               credentialsId: "${vars.JENKINS_CREDENTIALS}",
               url: "${vars.GIT_URL_TEST}"]
           ]
       ])

    if (body) { body() }
    } else {
    echo 'scm disabled, using shell!'
        // This is a workaround because of the timeout which cannot be extended in jenkins

    // TODO
    gitClone(GIT_BROWSE_URL_TEST, vars.relativeTargetDir)

    if (body) { body() }
  }
}
