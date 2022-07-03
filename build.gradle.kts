@file:Suppress("UnstableApiUsage")

buildscript {
  dependencies {
    classpath(libs.androidPluginForGradle)
    classpath(libs.kotlin.pluginForGradle)
  }
}

plugins {
  alias(libs.plugins.ktlint)
}

tasks.wrapper {
  distributionType = Wrapper.DistributionType.ALL
}

subprojects {
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks {
  register<Exec>("importStrings") {
    executable = "sh"
    args("-c", "tx pull -af --minimum-perc=5")
    finalizedBy(":app:lintDebug")
  }

  register<TestReport>("allUnitTests") {
    val tests = subprojects.mapNotNull { subProject ->
      val tasks = subProject.tasks
      (
        tasks.findByName("testDebugUnitTest")
          ?: tasks.findByName("test")
        ) as? Test
    }
    val artifactFolder = File("${rootDir.absolutePath}/artifacts")
    destinationDir = File(artifactFolder, "testResults")
    reportOn(tests)
  }
}
