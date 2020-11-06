package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency.DEFAULT_CONFIGURATION
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File


open class Update4jBundleCreator : DefaultTask() {


  lateinit var configuration: Update4jConfiguration

  @OutputDirectory
  var outputDirectory: File = File(project.buildDir, OUTPUT_DIRECTORY_DEFAULT)

  @TaskAction
  fun generateXml() {
    if (configuration.bundleLocation != null) {
      outputDirectory = File(project.projectDir, configuration.bundleLocation!!)
    }

    logger.debug("Bundle Location: $outputDirectory")
    val filesInThisVersion = mutableListOf<File>()

    // TODO: more generic way of pulling the artifact
    val artifactName = "${project.name}-${project.version}.jar"
    val projectArtifact = File(project.buildDir, "libs/$artifactName")
    val artifactTarget = File("${outputDirectory.absolutePath}/$artifactName")

    if (!projectArtifact.exists()) throw IllegalStateException("The artifact \"$projectArtifact\" does not exist!")
    logger.info("Copying artifact: $projectArtifact to $artifactTarget")
    projectArtifact.copyTo(artifactTarget, true)
    filesInThisVersion.add(artifactTarget)

    // TODO: make sure to handle "useMaven" option

    // add all other dependencies to the list
    project.configurations.getByName(DEFAULT_CONFIGURATION).forEach { artifact ->
      val dependencyArtifactTarget = File("${outputDirectory.absolutePath}/${artifact.name}")
      logger.info("Copying dependency: $artifact to $dependencyArtifactTarget")
      artifact.copyTo(dependencyArtifactTarget, true)
      filesInThisVersion.add(dependencyArtifactTarget)
    }

    // add resources
    configuration.resources
      .map { File(project.projectDir, it) }
      .forEach { file ->
        if (!file.exists()) {
          logger.warn("File \"${file.absolutePath}\" doesn't exist and will be ignored!")
        } else {
          val resourceTarget = File(outputDirectory.absolutePath, "${configuration.resourcesFolderName}/${file.name}")
          logger.info("Copying dependency: $file to $resourceTarget")
          file.copyTo(resourceTarget, true)
          filesInThisVersion.add(resourceTarget)
        }
      }

    val builder = Configuration.builder()
      .baseUri(configuration.remoteLocation)
      .basePath("\${user.dir}")
      .launcher(configuration.launcherClass)


    // TODO
//        if (configuration.signingKey != null) {
//            builder.signer(privateKey(requireNotNull(configuration.signingKey)))
//        }

    filesInThisVersion.forEach { file ->
      logger.info(file.absolutePath)
      builder.file(
        FileMetadata
          .readFrom(file.absolutePath)
          .uri(file.name)
          .classpath(file.name.endsWith(".jar"))
      )
    }

    // write output xml
    File("${outputDirectory.absolutePath}/${configuration.configurationFileName}")
      .writeText(builder.build().toString())
  }

  companion object {
    private const val OUTPUT_DIRECTORY_DEFAULT = "update4j"
  }
}
