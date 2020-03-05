package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.internal.artifacts.dependencies.AbstractDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.api.tasks.TaskAction
import java.io.File

open class Update4jXmlGenerator : DefaultTask() {

    private val output: String = ""
    @TaskAction
    fun generateXml() {
        println(project.rootDir)
        project.allprojects
            .flatMap { it.configurations }
            .first { it.name == "default" }
//            .filter { projectGenerator.includeProject(it) }
            .map { configuration ->
                try {
                    configuration.copyTo(File(project.rootDir, "build/${configuration.name}"))
                    println("Config: ${configuration}")
                } catch (error: Throwable) {

                }

            }
    }
}