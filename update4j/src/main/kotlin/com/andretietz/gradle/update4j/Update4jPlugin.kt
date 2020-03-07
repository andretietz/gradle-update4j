package com.andretietz.gradle.update4j

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class Update4jPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val configuration = project.extensions.create(
            "update4j",
            Update4jConfigurationExtension::class.java,
            project
        )

        project.tasks.create("generateConfiguration", Update4jBundleCreator::class.java, configuration)
            .dependsOn("build")
            .run {
                // Set description and group for the task
                description = "Generates the configuration file"
                group = "update4J"
            }

        project.tasks.create("clean-update4j") {
            File(project.rootDir, "build").delete()
        }.run {
            description = "Deletes the build folder"
            group = "update4J"
        }

//        project.tasks.getByName("clean").dependsOn(cleaningTask)
    }
}