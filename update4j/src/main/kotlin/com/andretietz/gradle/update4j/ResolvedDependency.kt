package com.andretietz.gradle.update4j

import org.update4j.OS
import java.io.File
import java.net.URL

sealed class ResolvedDependency(
  open val file: File
)

data class ExternalResolvedDependency(
  override val file: File,
  val url: URL,
  val os: OS,
  val needsCleanup: Boolean
) : ResolvedDependency(file)

data class LocalResolvedDependency(
  override val file: File
) : ResolvedDependency(file)



