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

    project.tasks.create(GRADLE_TASK_NAME, Update4jBundleCreator::class.java)
      .dependsOn("build")
      .run {
        // Set description and group for the task
        description = "Generates the configuration file"
        group = "update4j"
      }
  }
  companion object {
    private const val GRADLE_TASK_NAME = "generateBundle"
  }
}

internal fun Project.update4j(): Update4jConfiguration =
  extensions.getByName(EXTENSION_NAME) as? Update4jConfiguration
    ?: throw IllegalStateException("$EXTENSION_NAME is not of the correct type")
