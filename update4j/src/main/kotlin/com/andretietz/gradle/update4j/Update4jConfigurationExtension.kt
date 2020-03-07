package com.andretietz.gradle.update4j

import org.gradle.api.Project

open class Update4jConfigurationExtension(
    project: Project
) {
    var resources: List<String> = emptyList()
    var libraryFolderName: String? = "lib"
    var configurationFileName: String = "update.xml"
    var launcherClass: String? = null
    var remoteLocation: String? = null
    var bundleLocation: String = "${project.rootDir.absolutePath}/build/bundle"
}
