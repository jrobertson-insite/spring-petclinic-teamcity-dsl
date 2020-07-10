import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.project

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

val operatingSystems = listOf("Mac OS X", "Windows", "Linux")
val jdkVersions = listOf("JDK_18", "JDK_11")

project {
    for (os in operatingSystems) {
        for (jdk in jdkVersions) {
            buildType(wrapWithFeature(Build(os, jdk)){
                swabra {}
            })
        }
    }
}

//project {
//    vcsRoot(PetclinicVcs)
//    buildType(wrapWithFeature(Build){
//        swabra {}
//    })
//
//    subProject(TestProject)
//}

object TestProject : Project({
    name = "TestSubProject"
})

fun wrapWithFeature(buildType: BuildType, featureBlock: BuildFeatures.() -> Unit): BuildType {
    buildType.features {
        featureBlock()
    }
    return buildType
}

//object Build : BuildType({
class Build(val os: String, val jdk: String) : BuildType({
    //id("Build_${os}_${jdk}".toExtId())
    id("Build_${os}_${jdk}")
    name = "Build ($os, $jdk)"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean package"
            mavenVersion = defaultProvidedVersion()
            jdkHome = "%env.${jdk}%"
        }
    }

    requirements {
        equals("teamcity.agent.jvm.os.name", os)
    }
})

object PetclinicVcs : GitVcsRoot({
    name = "PetclinicVcs"
    url = "https://github.com/jrobertson-insite/spring-petclinic.git"
})
