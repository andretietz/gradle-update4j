package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.File
import javax.inject.Inject

open class Update4jBundleCreator @Inject constructor(
    configuration: Update4jConfigurationExtension
) : DefaultTask() {

    /**
     * This is the location where the bundle will be generated to.
     */
    private val bundleLocation = configuration.bundleLocation
    /**
     * name of the lib folder within the bundle/app/remote directory
     */
    private val libfolder: String? = configuration.libraryFolderName

    /**
     * Name of the configuration file
     */
    private val configName = configuration.configurationFileName

    /**
     * Remote bundle location (directory, in which the update.xml lives in)
     */
    private val remoteLocation: String? = configuration.remoteLocation

    /**
     * Class to start after update
     */
    private val launcher: String? = configuration.launcherClass

    private val resources: List<String> = configuration.resources


    @TaskAction
    fun generateXml() {
        if (remoteLocation == null) throw RuntimeException("Missing update4j.remoteLocation")
        if (launcher == null) throw RuntimeException("Missing update4j.launcherClass")

        val configurationBuilder = Configuration.builder()
            .baseUri(remoteLocation)
            .basePath("\${app.dir}")

        // TODO: this is super ugly and hacky!!!!!
        project.allprojects.forEach {
            val file = File("$bundleLocation/${project.name}-${project.version}.jar")
            if (!file.exists()) {
                try {
                    File(project.buildDir, "libs/${project.name}-${project.version}.jar").copyTo(file)
                } catch (error: Throwable) {
                    error.printStackTrace()
                }
            }

            configurationBuilder
                .file(
                    FileMetadata
                        .readFrom("$bundleLocation/${file.name}")
                        .uri(file.name)
                        .classpath()

                )

        }
        project.configurations.getByName("default")
//        project.allprojects
//            .flatMap { it.configurations }
//            .firstOrNull { it.name == "default" }
            .mapNotNull { dependency ->
                val targetFile = File("$bundleLocation/$libfolder/${dependency.name}")

                val file = if (!targetFile.exists()) {
                    dependency.copyTo(File("$bundleLocation/$libfolder/${dependency.name}"))
                } else {
                    targetFile
                }
                file
            }.let { list ->
                if (libfolder != null) {
                    configurationBuilder.property("app.lib", libfolder)
                }
                configurationBuilder.launcher(launcher)
                list.forEach { file ->
                    // TODO: add to manifest classpath
                    try {
                        configurationBuilder
                            .file(
                                FileMetadata
                                    .readFrom("$bundleLocation/$libfolder/${file.name}")
                                    .uri("\${app.lib}/${file.name}")
                                    .classpath()

                            )
                    } catch (error: Throwable) {
                        error.printStackTrace()
                    }
                }
                File("$bundleLocation/$configName").writeText(
                    configurationBuilder.build().toString()
                )
            }
    }
}