#!/usr/bin/groovy
import hudson.model.*

def call(Closure body=null) {
  this.vars = [:]
  call(vars, body)
}

def call(Map vars, Closure body=null) {
  echo '[JPL] Executing `vars/getMavenGoalsPitest.groovy`'

  vars = vars ?: [:]

  def DRY_RUN = vars.get('DRY_RUN', env.DRY_RUN ?: false).toBoolean()
  def RELEASE = vars.get('RELEASE', env.RELEASE ?: false).toBoolean()

  vars.skipPitest = vars.get('skipPitest', true).toBoolean()
  vars.mavenGoals = vars.get('mavenGoals', '')

  if (!vars.skipPitest && ((env.BRANCH_NAME == 'develop') || (env.BRANCH_NAME ==~ /PR-.*/) || (env.BRANCH_NAME ==~ /feature\/.*/) || (env.BRANCH_NAME ==~ /bugfix\/.*/))) {
    if (!DRY_RUN && !RELEASE) {
      echo 'pitest added'
      vars.mavenGoals += ' -DwithHistory'
      vars.mavenGoals += ' org.pitest:pitest-maven:mutationCoverage'
    }
    } // if DRY_RUN

  if (body) { body() }

  return vars.mavenGoals
}
