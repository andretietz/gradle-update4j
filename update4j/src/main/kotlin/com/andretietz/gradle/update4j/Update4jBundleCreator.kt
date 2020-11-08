package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency.DEFAULT_CONFIGURATION
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.DefaultResolvedArtifact
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File
import java.io.InputStream
import java.net.URI


open class Update4jBundleCreator : DefaultTask() {

  @Input
  lateinit var update4jConfigurationFile: String

  @Input
  lateinit var launcherClass: String

  @Input
  lateinit var remoteLocation: String

  @Input
  lateinit var targetDirectory: String

  @Input
  lateinit var resourcesDirectoryName: String

  @Input
  lateinit var resources: List<String>

  @Input
  var useMaven: Boolean = true

  @OutputDirectory
  var outputDirectory: File = File(project.buildDir, OUTPUT_DIRECTORY_DEFAULT)

  @TaskAction
  fun generateXml() {
    outputDirectory = File(project.buildDir, targetDirectory)
    logger.debug("Bundle Location: $outputDirectory")

    // TODO: Check for valid inputs!
    val fileReferences = mutableListOf<FileMetadata.Reference>()

    @Suppress("UnstableApiUsage")
    val repos = project.repositories
      .filterIsInstance<MavenArtifactRepository>()
      // make sure to exclude maven local, since it cannot be reached
      .filter { it.name != "MavelLocal" && it.url.scheme != "file" }
      .map { it.name to it.url }
      .toMap()

    // copy all artifacts from this project into the output dir
    project.configurations.getByName(DEFAULT_CONFIGURATION).allArtifacts
      .map { it.file }.forEach { artifact ->
        val targetFile = File("${outputDirectory.absolutePath}/${artifact.name}")
        artifact.copyTo(targetFile, true)
        fileReferences.add(
          FileMetadata.readFrom(artifact.absolutePath)
            .uri(artifact.name)
            .classpath(artifact.name.endsWith(".jar"))
        )
      }

    val dependencies = project.configurations.getByName(DEFAULT_CONFIGURATION)
      .resolvedConfiguration.firstLevelModuleDependencies
      .flatMap { handleDependency(it) }.toSet()

    dependencies.forEach { dependency ->
      if (dependency is DefaultResolvedArtifact) {
        var handled = false
        for ((_, repo) in repos) {
          // This is a bit... optimistic...
          val group = dependency.moduleVersion.id.group
          val name = dependency.moduleVersion.id.name
          val version = dependency.moduleVersion.id.version
          val classifier =
            if (dependency.artifactName.classifier != null) "-${dependency.artifactName.classifier}" else ""

          val url = URI(
            String.format(
              "%s%s/%s/%s/%s-%s%s.%s",
              repo.toString(),
              group.replace('.', '/'),
              name, version, name, version,
              classifier,
              dependency.artifactName.extension ?: dependency.artifactName.type
            )
          )

          var stream: InputStream? = null
          try {
            stream = url.toURL().openStream()
            if (stream != null) {
              logger.debug("Found External dependency: $url")
              fileReferences.add(
                FileMetadata
                  .readFrom(dependency.file.absolutePath)
                  .uri(url)
                  .classpath(dependency.file.name.endsWith(".jar"))
              )
              handled = true
              break
            }
          } catch (error: Throwable) {
            logger.trace("Couldn't find dependency here: $url")
          } finally {
            stream?.close()
          }
        }

        if (!handled) {
          val dependencyArtifactTarget = File("${outputDirectory.absolutePath}/${dependency.file.name}")
          logger.info("Copying dependency: ${dependency.file} to $dependencyArtifactTarget")
          dependency.file.copyTo(dependencyArtifactTarget, true)
          fileReferences.add(
            FileMetadata
              .readFrom(dependencyArtifactTarget.absolutePath)
              .uri(dependencyArtifactTarget.name)
              .classpath(dependency.file.name.endsWith(".jar"))
          )
        }

      } else {
        logger.warn("UNKNOWN dependency type: ${dependency.javaClass.canonicalName}")
      }
    }

    // add resources
    resources
      .map { File(project.projectDir, it) }
      .forEach { file ->
        if (!file.exists()) {
          logger.warn("File \"${file.absolutePath}\" doesn't exist and will be ignored!")
        } else {
          val resourceTarget = File(outputDirectory.absolutePath, "$resourcesDirectoryName/${file.name}")
          logger.info("Copying dependency: $file to $resourceTarget")
          if (resourceTarget.isFile) {
            file.copyTo(resourceTarget, true)
            fileReferences.add(
              FileMetadata
                .readFrom(resourceTarget.absolutePath)
                .uri(resourceTarget.name)
            )
          } else if (resourceTarget.isDirectory) {
            // TODO
            logger.warn("directory resources not supported atm! (tried copying: ${resourceTarget.absolutePath})")
//            file.walkTopDown().forEach { dirFile ->
//              if (dirFile.isFile) {
//                fileReferences.add(
//                  FileMetadata
//                    .readFrom(dirFile.absolutePath)
//                    .uri(dirFile.relativeTo(project.buildDir).toString())
//                )
//              }
//            }
          }
        }
      }


    // generate xml file
    val builder = Configuration.builder()
      .baseUri(remoteLocation)
      .basePath("\${user.dir}")
      .launcher(launcherClass)


    fileReferences.forEach {
      builder.file(it)
    }

    for (file in builder.files) {
      println(file.uri)
    }
    // write output xml
    File("${outputDirectory.absolutePath}/$update4jConfigurationFile")
      .writeText(builder.build().toString())
  }

  fun handleDependency(
    dependency: ResolvedDependency,
    artifacts: MutableSet<ResolvedArtifact> = mutableSetOf()
  ): Set<ResolvedArtifact> {
    dependency.moduleArtifacts.forEach {
      artifacts.add(it)
    }
    dependency.children.forEach { handleDependency(it, artifacts) }
    return artifacts
  }

  companion object {
    private const val OUTPUT_DIRECTORY_DEFAULT = "update4j"
  }
}
