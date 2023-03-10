package com.test.jenkins

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library

import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource
import static com.lesfurets.jenkins.unit.MethodSignature.method

import static org.assertj.core.api.Assertions.assertThat

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

import com.lesfurets.jenkins.unit.MethodCall
import com.test.jenkins.unit.BasePipelineRegressionTest

@RunWith(Parameterized)
class TestJenkinsFile extends BasePipelineRegressionTest {

    @Rule
  public TemporaryFolder folder = new TemporaryFolder()

  String sharedLibs = this.class.getResource('/libs').getFile()

    @Parameterized.Parameter
  public String branch
    @Parameterized.Parameter(1)
  public String expectedPhase

    @Parameterized.Parameters(name = 'Test branch {0} phase {1}')
  static Collection<Object[]> data() {
    return [
            ['develop', 'develop'],
            ['master', 'master'],
            ['feature_', 'feature_']
        ].collect { it as Object[] }
  }

    @Override
    @Before
  void setUp() throws Exception {
    super.setUp()

        //binding.setVariable('BRANCH_NAME' , 'develop')

        //def scmBranch = branch
        //binding.setVariable('scm', [branch: 'master'])
        /*
         binding.setVariable('scm', [
         $class                           : 'GitSCM',
         branches                         : [[name: scmBranch]],
         doGenerateSubmoduleConfigurations: false,
         extensions                       : [],
         submoduleCfg                     : [],
         userRemoteConfigs                : [
         [
         credentialsId: 'gitlab_git_ssh',
         url          : 'github.com/lesfurets/JenkinsPipelineUnit.git'
         ]]
         ])
         */

    def library = library().name('jenkins-pipeline-scripts')
                .defaultVersion('master')
                .allowOverride(true)
                .implicit(false)
                .targetPath(sharedLibs)
                .retriever(localSource(sharedLibs))
                .build()
    helper.registerSharedLibrary(library)

    //binding.setVariable('myenv' , '')
    helper.registerAllowedMethod('myenv', [Map], { c -> 'myenv' })
    helper.registerAllowedMethod('getPropertyList', [], { c -> [] })

    helper.registerAllowedMethod('defineEnvironment', [], null)
    helper.registerAllowedMethod('printEnvironment', [], null)

    binding.setVariable('env', '')
    //binding.setVariable('env', [BRANCH_NAME: 'master'])
    //helper.registerAllowedMethod("env", [Map.class], {c -> "env"})
    //helper.registerAllowedMethod("getEnvironment", [], null)
    //registerAllowedMethod(methodSignature, callback != null ? { params -> return callback.apply(params) } : null)
    //helper.registerAllowedMethod(method("getEnvironment",  Map.class),  { map ->
    //    return "0"
    //})
    //binding.setVariable('BUILD_TEST', true)
    //binding.setVariable('BUILD_ONLY', true)
    //binding.setVariable('BUILD_GRADLE', true)
    binding.setVariable('env', [getEnvironment: [COMPOSE_HTTP_TIMEOUT: '200']])
    binding.getVariable('env').getEnvironment = [COMPOSE_HTTP_TIMEOUT: '200']

    helper.registerAllowedMethod('createPropertyList', [], { c ->
      [
                buildDiscarder(
                logRotator(
                daysToKeepStr:         30,
                numToKeepStr:          2,
                artifactDaysToKeepStr: 10,
                artifactNumToKeepStr:  1
                )
                )
            ]})
        //helper.registerAllowedMethod("createPropertyList", [Map.class], null)
        //helper.registerAllowedMethod("createPropertyList", [Map.class, Closure.class], null)

    helper.registerAllowedMethod('setBuildName', [], null)
    helper.registerAllowedMethod('setBuildName', [String], null)

    helper.registerAllowedMethod('sh', [Map], { c -> 'build.sh' })

    //For debuging
    helper.registerAllowedMethod('getEnvironementData', [], null)
    helper.registerAllowedMethod('getEnvironementData', [Map], null)

    helper.registerAllowedMethod('withMavenWrapper', [Map], null)
    helper.registerAllowedMethod('withMavenWrapper', [Map, Closure], null)

    helper.registerAllowedMethod('withShellCheckWrapper', [Map], null)
    helper.registerAllowedMethod('withShellCheckWrapper', [Map, Closure], null)

    helper.registerAllowedMethod('withSonarQubeWrapper', [Map], null)
    helper.registerAllowedMethod('withSonarQubeWrapper', [Map, Closure], null)

    helper.registerAllowedMethod('withCheckmarxWrapper', [Map], null)
    helper.registerAllowedMethod('withCheckmarxWrapper', [Map, Closure], null)

    helper.registerAllowedMethod('sshPublisherWrapper', [Map], null)

    helper.registerAllowedMethod('wrapCleanWs', [], null)
    helper.registerAllowedMethod('wrapCleanWs', [Map], null)
    helper.registerAllowedMethod('wrapCleanWs', [Map, Closure], null)
    helper.registerAllowedMethod('wrapCleanWsOnNode', [], null)
    helper.registerAllowedMethod('wrapCleanWsOnNode', [Map], null)
    helper.registerAllowedMethod('wrapCleanWsOnNode', [Map, Closure], null)

    helper.registerAllowedMethod('getDockerOpts', [], null)
    helper.registerAllowedMethod('getDockerOpts', [Map], { c ->'-v /home/jenkins:/home/jenkins -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro -v /var/run/docker.sock:/var/run/docker.sock' })

    helper.registerAllowedMethod('getJenkinsOpts', [], null)
    helper.registerAllowedMethod('getJenkinsOpts', [Map], { c ->'true' })

    helper.registerAllowedMethod('cleanStash', [], null)
    helper.registerAllowedMethod('cleanStash', [Map], null)
    helper.registerAllowedMethod('cleanStash', [Map, Closure], null)

    helper.registerAllowedMethod('dockerTag', [], { c ->'test' })
    helper.registerAllowedMethod('dockerTag', [String], { c ->'test' })
    helper.registerAllowedMethod('dockerTag', [Map], { c ->'test' })

    helper.registerAllowedMethod('dockerImageTag', [], { c ->'latest' })
    helper.registerAllowedMethod('dockerImageTag', [String], { c ->'latest' })
    helper.registerAllowedMethod('dockerImageTag', [Map], { c ->'latest' })

    helper.registerAllowedMethod('dockerHadoLint', [], null)
    helper.registerAllowedMethod('dockerHadoLint', [Map], null)

    helper.registerAllowedMethod('dockerLint', [], null)
    helper.registerAllowedMethod('dockerLint', [Map], null)

    helper.registerAllowedMethod('taskScanner', [], null)
    helper.registerAllowedMethod('taskScanner', [Map], null)
    helper.registerAllowedMethod('tagList', [], null)
    helper.registerAllowedMethod('tagList', [Map], null)

    helper.registerAllowedMethod('getDockerProxyOpts', [], null)
    helper.registerAllowedMethod('getDockerProxyOpts', [Map], null)

    helper.registerAllowedMethod('withLogParser', [], null)
    helper.registerAllowedMethod('withLogParser', [Map], null)

    //helper.registerAllowedMethod("draftStage", [], null)
    helper.registerAllowedMethod('draftStage', [Map], null)

    helper.registerAllowedMethod('k8sConfig', [], null)
    helper.registerAllowedMethod('k8sConfig', [Map], null)
    helper.registerAllowedMethod('k8sNamespace', [], null)
    helper.registerAllowedMethod('k8sNamespace', [Map], null)

    helper.registerAllowedMethod('helmLint', [], null)
    helper.registerAllowedMethod('helmLint', [Map], null)
    helper.registerAllowedMethod('helmDeploy', [], null)
    helper.registerAllowedMethod('helmDeploy', [Map], null)
    helper.registerAllowedMethod('helmDelete', [], null)
    helper.registerAllowedMethod('helmDelete', [Map], null)
    helper.registerAllowedMethod('helmInstall', [], null)
    helper.registerAllowedMethod('helmInstall', [Map], null)
    helper.registerAllowedMethod('helmDependency', [], null)
    helper.registerAllowedMethod('helmDependency', [Map], null)
    helper.registerAllowedMethod('helmTest', [], null)
    helper.registerAllowedMethod('helmTest', [Map], null)
    helper.registerAllowedMethod('helmPush', [], null)
    helper.registerAllowedMethod('helmPush', [Map], null)

    helper.registerAllowedMethod('isReleaseBranch', [], null)
    helper.registerAllowedMethod('dockerComposeLogs', [], null)

    helper.registerAllowedMethod('withRegistryWrapper', [], null)
    helper.registerAllowedMethod('withRegistryWrapper', [Map], null)
    // dockerPush to be used wrapped with withRegistryWrapper for draftStage
    helper.registerAllowedMethod('dockerPush', [Map], null)

    //Do no use below deprecated method
    //helper.registerAllowedMethod("abortPreviousRunningBuilds", [], null)
    //helper.registerAllowedMethod("cleanCaches", [], null)
    //helper.registerAllowedMethod("cleanDockerConfig", [], null)
    //helper.registerAllowedMethod("dockerCleaning", [], null)
    //helper.registerAllowedMethod("doDockerImagePush", [], null)
    //helper.registerAllowedMethod("getDefaultCheckoutBranches", [], null)
    //helper.registerAllowedMethod("getDefaultCheckoutExtensions", [], null)
    //helper.registerAllowedMethod("gitCheckoutALF", [], null)
    //helper.registerAllowedMethod("gitCheckoutALFRepo", [], null)
    //helper.registerAllowedMethod("gitCheckoutARC", [], null)
    //helper.registerAllowedMethod("gitCheckoutARCRepo", [], null)
    //helper.registerAllowedMethod("gitCheckoutBMRepo", [], null)
    //helper.registerAllowedMethod("gitCheckoutTEST", [], null)
    //helper.registerAllowedMethod("gitCheckoutTESTRepo", [], null)
    //helper.registerAllowedMethod("wrapCleanWs", [], null) // use wrapCleanWsOnNode instead
    //helper.registerAllowedMethod("wrapCleanWs", [Map.class], null) // use wrapCleanWsOnNode instead
    //helper.registerAllowedMethod("wrapCleanWs", [Map.class, Closure.class], null) // use wrapCleanWsOnNode instead
    helper.registerAllowedMethod('runHtmlPublishers', [List], null)

    //Do no use below method directly
    //Do no use draftPack directly for instance it is run by draftStage
    //helper.registerAllowedMethod("draftPack", [Map.class], null)
    //helper.registerAllowedMethod("withConfigFileProviderWrapper", [Map.class], null)
    //helper.registerAllowedMethod("helmTag", [], null)
    //helper.registerAllowedMethod("dockerCheckHealth", [], null)
    //helper.registerAllowedMethod("dockerWaitHealth", [], null)
    //helper.registerAllowedMethod("doDockerImagePush", [], null)
    //helper.registerAllowedMethod("getCommitRevision", [], null)
    //helper.registerAllowedMethod("getCommitSha", [], null)
    //helper.registerAllowedMethod("getCommitShortSHA1", [], null)
    //helper.registerAllowedMethod("getCommitId", [], null)
    //helper.registerAllowedMethod("getSemVerReleasedVersion", [], null)
    //helper.registerAllowedMethod("getShortReleasedVersion", [], null)
    //helper.registerAllowedMethod("getCurrentDate", [], null)
    //helper.registerAllowedMethod("getJenkinsOpts", [], null)
    //helper.registerAllowedMethod("getTimestamp", [], null)
    //helper.registerAllowedMethod("pushDockerImage", [], null) use dockerPush instead
    //helper.registerAllowedMethod("gitTagLocal", [], null)
    //helper.registerAllowedMethod("gitTagRemote", [], null)
    //helper.registerAllowedMethod("createVersionTextFile", [], null) use createManifest instead

    helper.registerAllowedMethod('triggeredBy', [String], null)
    helper.registerAllowedMethod('triggeredBy', [String], { c ->'TimerTrigger' })
  }

    @Test
  void name() throws Exception {
    binding.getVariable('env').BRANCH_NAME = "$expectedPhase"

    runScript('Jenkinsfile')
    super.testNonRegression(branch)
    assertThat(helper.callStack.stream()
                .filter { c ->
                  c.methodName == 'sh'
                }
                .map(MethodCall.&callArgsToString)
                .findAll { s -> s.contains("echo TEST : $expectedPhase") })
                .hasSize(1)
  }

}
