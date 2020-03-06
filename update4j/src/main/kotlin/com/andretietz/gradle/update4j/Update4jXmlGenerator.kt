package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.update4j.Configuration
import org.update4j.FileMetadata
import org.update4j.OS
import org.update4j.Property
import java.io.File

open class Update4jXmlGenerator : DefaultTask() {

    /**
     * TODO: load from gradle config (optional)
     * This is the location where the bundle will be generated to.
     */
    private val bundleLocation = "${project.rootDir.absolutePath}/build/bundle"
    /**
     * TODO: load from gradle config (optional)
     * name of the lib folder within the bundle/app/remote directory
     */
    private val libfolder: String = "lib"

    /**
     * TODO: load from gradle config (optional)
     * Name of the configuration file
     */
    private val configName = "update.xml"

    /**
     * TODO: load from gradle config
     * Remote bundle location (directory, in which the update.xml lives in)
     */
    private val remoteLocation: String = "http://www.mediaav.de/mau-update"

    /**
     * TODO: load from gradle config
     * Name of the application
     */
    private val appName: String = "App Name"

    /**
     * TODO: load from gradle config
     * Class to start after update
     */
    private val launcher: String = "com.andretietz.updater.Application"


    @TaskAction
    fun generateXml() {
        val configurationBuilder = Configuration.builder()
            .baseUri(remoteLocation)
            .basePath("\${app.dir}")

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
        project.allprojects
            .flatMap { it.configurations }
            .firstOrNull { it.name == "default" }
            ?.mapNotNull { dependency ->
                val targetFile = File("$bundleLocation/$libfolder/${dependency.name}")

                val file = if (!targetFile.exists()) {
                    dependency.copyTo(File("$bundleLocation/$libfolder/${dependency.name}"))
                } else {
                    targetFile
                }
                println("File \"$file\" exists: ${file.exists()}")
                file
            }?.let { list ->
                configurationBuilder.properties(
                    listOf(
                        Property("app.name", appName),
                        // Install location
                        Property("app.dir", "\${user.dir}/\${app.name}"),
                        Property("app.dir", "\${LOCALAPPDATA}/\${app.name}", OS.WINDOWS),
                        Property("app.lib", "\${app.dir}/$libfolder")
                    )
                )
                    .launcher(launcher)
                list.forEach { file ->
                    try {
                        configurationBuilder
                            .file(
                                FileMetadata
                                    .readFrom("$bundleLocation/$libfolder/${file.name}")
                                    .uri("$libfolder/${file.name}")
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