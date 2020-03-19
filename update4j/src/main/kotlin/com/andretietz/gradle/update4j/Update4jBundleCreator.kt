package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency.ARCHIVES_CONFIGURATION
import org.gradle.api.artifacts.Dependency.DEFAULT_CONFIGURATION
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File

open class Update4jBundleCreator : DefaultTask() {

    @TaskAction
    fun generateXml() {
        val configuration = project.update4j()
        val bundleLocation = if (configuration.bundleLocation != null)
            File(project.projectDir, configuration.bundleLocation!!).absolutePath
        else
            File(project.buildDir, "update4j").absolutePath


        val libraryFolder = if (configuration.libraryFolderName != null)
            "${bundleLocation}/${configuration.libraryFolderName}/"
        else
            File(bundleLocation).absolutePath

        val filesInThisVersion = mutableListOf<File>()

        project.configurations.getByName(ARCHIVES_CONFIGURATION).forEach {
            logger.warn(it.toString())
        }
        File("$bundleLocation/${project.name}-${project.version}.jar").run {
            if (!exists()) File(project.buildDir, "libs/${project.name}-${project.version}.jar")
                .copyTo(this)
            filesInThisVersion.add(this)
        }
        project.configurations.getByName(DEFAULT_CONFIGURATION).forEach { file ->
            File("$libraryFolder/${file.name}").run {
                if (!exists()) file.copyTo(this)
                filesInThisVersion.add(this)
            }
        }

        configuration.resources.map { File(bundleLocation, it) }.forEach { file ->
            if (!file.exists()) {
                logger.warn("File \"${file.absolutePath}\" doesn't exist and will be ignored!")
            } else {
                filesInThisVersion.add(
                    file.copyTo(
                        File(
                            bundleLocation,
                            "${configuration.resourcesFolderName}/${file.name}"
                        )
                    )
                )
            }
        }

        val builder = Configuration.builder()
            .baseUri(configuration.remoteLocation)
            .launcher(configuration.launcherClass)

        filesInThisVersion.forEach { file ->
            logger.warn(file.absolutePath)
            builder.file(
                FileMetadata
                    .readFrom(file.absolutePath)
                    .uri(file.absolutePath.replace(bundleLocation, "\${user.dir}"))
                    .classpath(file.name.endsWith(".jar"))
            )
        }
        File("$bundleLocation/${configuration.configurationFileName}").writeText(
            builder.build().toString()
        )
    }

}