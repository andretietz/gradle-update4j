package com.andretietz.gradle.update4j

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "update4j"

class Update4jPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.extensions.create(
      EXTENSION_NAME,
      Update4jConfiguration::class.java
    )

    project.tasks.register(GRADLE_GENERATE_TASK_NAME, Update4jBundleCreator::class.java) { task ->
      task.configuration = project.update4j()
      task.group = "update4j"
      task.dependsOn(project.tasks.getByName("build"))


    }
  }

  companion object {
    private const val GRADLE_GENERATE_TASK_NAME = "generateBundle"
    private const val GRADLE_CLEAN_TASK_NAME = "clean"
    private const val OUTPUT_DIRECTORY_DEFAULT = "update4j"
  }
}

internal fun Project.update4j() = extensions.getByName(EXTENSION_NAME) as Update4jConfiguration

//class Update4jPrepare(private val project: Project) : Action<Update4jBundleCreator> {
//  override fun execute(task: Update4jBundleCreator) {
//    project.update4j()
//  }
//}
