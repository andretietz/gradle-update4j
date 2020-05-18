package com.andretietz.gradle.update4j

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency.DEFAULT_CONFIGURATION
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.org.bouncycastle.crypto.util.OpenSSHPrivateKeyUtil
import org.update4j.Configuration
import org.update4j.FileMetadata
import java.io.DataInputStream
import java.io.File
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec


open class Update4jBundleCreator : DefaultTask() {

    @TaskAction
    fun generateXml() {
        val configuration = project.update4j()

        val versionSubFolder = if (configuration.remoteVersionSubfolder != null) {
            var folder = requireNotNull(configuration.remoteVersionSubfolder).trim()
            if (folder.endsWith("/")) folder = folder.substring(0, folder.length - 1)
            folder.format(project.version.toString()) + "/"
        } else ""

        // get the location in which the whole bundle will be put in
        val bundleLocation = if (configuration.bundleLocation != null) {
            File(project.projectDir, configuration.bundleLocation!!).absolutePath
        } else {
            File(project.buildDir, "update4j").absolutePath
        }.apply {

        }
        logger.debug("Bundle Location: $bundleLocation")

        logger.error(project.version.toString())

//        val versionSubFolder = if (configuration.remoteVersionSubfolder != null) {
//            var folder = requireNotNull(configuration.remoteVersionSubfolder)
//            if (!folder.endsWith("/")) folder = "$folder/"
//            folder.format(project.version.toString())
//        } else "/"

        val filesInThisVersion = mutableListOf<File>()

//        project.configurations.getByName(ARCHIVES_CONFIGURATION).forEach {
//            logger.warn(it.toString())
//        }

        // Add the main artifact to the list
        // TODO: find better way to get the artifact name
        File("$bundleLocation/${project.name}-${project.version}.jar").run {
            if (!exists()) File(project.buildDir, "libs/${project.name}-${project.version}.jar")
                .copyTo(this)
            filesInThisVersion.add(this)
        }

        // add all other dependencies to the list
        // TODO: make sure to handle "useMaven" option
        project.configurations.getByName(DEFAULT_CONFIGURATION).forEach { file ->
            File("$bundleLocation/${file.name}").run {
                if (!exists()) file.copyTo(this)
                filesInThisVersion.add(this)
            }
        }

        // add resources
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
            .basePath("\${user.dir}")
            .launcher(configuration.launcherClass)


        // TODO
//        if (configuration.signingKey != null) {
//            builder.signer(privateKey(requireNotNull(configuration.signingKey)))
//        }

        filesInThisVersion.forEach { file ->
            logger.warn(file.absolutePath)
            builder.file(
                FileMetadata
                    .readFrom(file.absolutePath)
                    .uri(file.name)
                    .classpath(file.name.endsWith(".jar"))
            )
        }
        File("$bundleLocation/${configuration.configurationFileName}")
            .writeText(builder.build().toString())
    }


}
