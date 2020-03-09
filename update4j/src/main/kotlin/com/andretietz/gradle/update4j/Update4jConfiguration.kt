package com.andretietz.gradle.update4j

open class Update4jConfiguration @JvmOverloads constructor(
    var resourcesFolderName: String = "res",
    var resources: List<String> = emptyList(),
    var libraryFolderName: String? = "lib",
    var configurationFileName: String = "update.xml",
    var launcherClass: String? = null,
    var remoteLocation: String? = null,
    var bundleLocation: String? = null
) {
    override fun toString(): String {
        return """
            libraryFolderName: $libraryFolderName,
            configurationFileName: $configurationFileName,
            launcherClass: $launcherClass,
            remoteLocation: $remoteLocation,
            bundleLocation: $bundleLocation,
            resourcesFolderName: $resourcesFolderName,
            resources: $resources
        """.trimIndent()
    }
}