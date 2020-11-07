package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.Dependency.DEFAULT_CONFIGURATION
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.DefaultResolvedArtifact
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File
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
    val localFiles = mutableListOf<File>()
    val removeFiles = mutableListOf<File>()
    val remoteFiles = mutableMapOf<String, MutableList<URI>>()


    // copy all artifacts from this project into the output dir
    project.configurations.getByName(DEFAULT_CONFIGURATION).allArtifacts
      .map { it.file }.forEach { artifact ->
        val targetFile = File("${outputDirectory.absolutePath}/${artifact.name}")
        artifact.copyTo(targetFile, true)
        localFiles.add(targetFile)
      }


    @Suppress("UnstableApiUsage")
    val repos = project.repositories
      .filterIsInstance<MavenArtifactRepository>()
      .map { it.name to it.url }
      .toMap()


    val dependencies = mutableSetOf<ResolvedArtifact>()
    // DefaultResolvedDependency
    project.configurations.getByName(DEFAULT_CONFIGURATION).resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
      handleDependency(dependency, dependencies)
    }

    dependencies.forEach { dependency ->
      if(dependency is DefaultResolvedArtifact) {
        for ((repoName, repo) in repos) {
          // This is a bit... optimistic...
          val group = dependency.moduleVersion.id.group
          val name = dependency.moduleVersion.id.name
          val version = dependency.moduleVersion.id.version
          val url = URI(
            String.format(
              "%s%s/%s/%s/%s-%s.%s",
              repo.toString(),
              group.replace('.', '/'),
              name, version, name, version,
              dependency.artifactName.extension ?: dependency.artifactName.type
            )
          )
          try {
            val stream = url.toURL().openStream()
            if (stream != null) {
              println("External dependency: $url")
              val list = remoteFiles.getOrDefault(repoName, mutableListOf())
              list.add(url)
              remoteFiles[repoName] = list
              break
            }
          } catch (error: Throwable) {
            logger.trace("Couldn't find dependency here: $url")
          }
        }
      } else {
        println("UNKNOWN dependency type!: ${dependency.javaClass.canonicalName}")
      }


    }


    // add all other dependencies to the list
    project.configurations.getByName(DEFAULT_CONFIGURATION).forEach { artifact ->
      val fromMaven = remoteFiles.values.any { list -> list.any { it.path.endsWith(artifact.name) } }
      val dependencyArtifactTarget = File("${outputDirectory.absolutePath}/${artifact.name}")
      logger.info("Copying dependency: $artifact to $dependencyArtifactTarget")
      artifact.copyTo(dependencyArtifactTarget, true)
      if (!fromMaven) {
        localFiles.add(dependencyArtifactTarget)
      } else {
        removeFiles.add(dependencyArtifactTarget)
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
          file.copyTo(resourceTarget, true)
          localFiles.add(resourceTarget)
        }
      }


    // generate xml file

    val builder = Configuration.builder()
      .baseUri(remoteLocation)
      .basePath("\${user.dir}")
      .launcher(launcherClass)

    repos.keys.filter { remoteFiles.keys.contains(it) }.forEach {
      builder.property(it, repos[it].toString())
    }


    remoteFiles.forEach { (_, items) ->
      items.forEach { uri ->
        val tmpFile = removeFiles.first { uri.path.endsWith(it.name) }
        builder.file(
          FileMetadata
            .readFrom(tmpFile.absolutePath)
            .uri(uri)
            .classpath(tmpFile.name.endsWith(".jar"))
        )
      }
    }

    localFiles.forEach { file ->
      builder.file(
        FileMetadata
          .readFrom(file.absolutePath)
          .uri(file.name)
          .classpath(file.name.endsWith(".jar"))
      )
    }

    // write output xml
    File("${outputDirectory.absolutePath}/$update4jConfigurationFile")
      .writeText(builder.build().toString())

    removeFiles.forEach { it.delete() }
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

  sealed class Dep(val localFile: File) {
    class MavenDep(localFile: File) : Dep(localFile)
  }


  data class DependencyInformation(
    val localFile: File,
  )


  companion object {
    private const val OUTPUT_DIRECTORY_DEFAULT = "update4j"
  }
}
