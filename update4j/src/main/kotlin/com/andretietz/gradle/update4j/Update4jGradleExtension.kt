package com.andretietz.gradle.update4j

import org.gradle.api.Project

open class Update4jGradleExtension(
    val project: Project
) {
    var resources: List<String> = emptyList()
    var libraryFolderName: String = "lib"
    var configurationFileName: String = "update.xml"
    lateinit var launcherClass: String
    lateinit var remoteLocation: String
}
