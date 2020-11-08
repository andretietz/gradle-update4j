package com.andretietz.gradle.update4j

import java.io.Serializable

open class Update4jConfiguration @JvmOverloads constructor(
  var resourcesFolderName: String = "res",
  var resources: List<String> = listOf(),
  var libraryFolderName: String? = null,
  var configurationFileName: String = "update.xml",
  var useMaven: Boolean = true,
  var signingKey: String? = null,
  var launcherClass: String? = null,
  var remoteLocation: String? = null,
  var bundleLocation: String = OUTPUT_DIRECTORY_DEFAULT
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

  companion object {
    private const val OUTPUT_DIRECTORY_DEFAULT = "update4j"
  }
}
