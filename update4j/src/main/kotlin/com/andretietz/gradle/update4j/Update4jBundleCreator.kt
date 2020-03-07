package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File

open class Update4jBundleCreator : DefaultTask() {

    @TaskAction
    fun generateXml() {
        val configuration = project.update4j()
        val bundleLocation = if (configuration.bundleLocation != null)
            File(project.projectDir, configuration.bundleLocation!!).path
        else
            File(project.buildDir, "update4j").path

        logger.error(configuration.toString())

        val libraryFolder = if (configuration.libraryFolderName != null)
            "${configuration.bundleLocation}/${configuration.libraryFolderName}/"
        else
            configuration.bundleLocation


        val filesInThisVersion = mutableListOf<File>()

        val applicationFile = File("$bundleLocation/${project.name}-${project.version}.jar")

        filesInThisVersion.add(
            if (applicationFile.exists()) applicationFile else
                File(project.buildDir, "libs/${project.name}-${project.version}.jar").copyTo(
                    applicationFile
                )
        )

        project.configurations.getByName("default").forEach { file ->

            filesInThisVersion.add(file.copyTo(File("$libraryFolder/${file.name}")))
        }

        configuration.resources.map { File(it) }.forEach { file ->
            if (file.exists()) {
                filesInThisVersion.add(
                    file.copyTo(
                        File(
                            bundleLocation,
                            "${configuration.resourcesFolderName}/${file.name}"
                        )
                    )
                )
            } else {
                logger.warn("File {} doesn't exist.", file.absolutePath)
            }

        }

        val builder = Configuration.builder()
            .baseUri(configuration.remoteLocation)
            .launcher(configuration.launcherClass)

        filesInThisVersion.forEach { file ->
            builder.file(
                FileMetadata
                    .readFrom(file.absolutePath)
                    .uri(file.absolutePath)
                    .classpath(file.name.endsWith(".jar"))
            )
        }
        File("$bundleLocation/${configuration.configurationFileName}").writeText(
            builder.build().toString()
        )
    }

}