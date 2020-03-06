package com.andretietz.updater

import org.update4j.Configuration
import java.io.File
import java.io.FileReader


class Bootstrap {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Start Application ")
            val configuration = Configuration.read(FileReader(File("update.xml")))
            if (configuration.requiresUpdate()) {
                configuration.update()
            }
            configuration.launch()

        }
    }
}