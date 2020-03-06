package com.andretietz.updater

import org.update4j.LaunchContext
import org.update4j.service.Launcher


class Application : Launcher {
    override fun run(context: LaunchContext) {
        println("Hello World")
    }
}