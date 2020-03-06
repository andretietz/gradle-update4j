package com.andretietz.gradle.update4j

import org.gradle.api.Plugin
import org.gradle.api.Project

class Update4jPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "update4j",
            Update4jGradleExtension::class.java,
            project
        )

        project.tasks.create("generateConfiguration", Update4jXmlGenerator::class.java)
            .dependsOn("build")
            .run {
                // Set description and group for the task
                description = "Generates the configuration file"
                group = "Update4J"
            }

        // TODO
        project.tasks.getByName("clean").dependsOn(
            project.tasks.create("clean-gradle-update4j") {

            }
        )
    }
}