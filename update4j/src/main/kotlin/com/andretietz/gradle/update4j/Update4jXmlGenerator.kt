package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class Update4jXmlGenerator : DefaultTask() {
    @TaskAction
    fun generateXml() {
        println("Hello World!")
    }
}