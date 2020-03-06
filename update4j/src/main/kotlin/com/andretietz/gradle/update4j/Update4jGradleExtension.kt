package com.andretietz.gradle.update4j

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class Update4jGradleExtension(project: Project) {
    var data: NamedDomainObjectContainer<Update4jConfiguration> = project
        .container(Update4jConfiguration::class.java) {
            Update4jConfiguration(it)
        }.apply {
            add(Update4jConfiguration.ALL)
        }
}

data class Update4jConfiguration @JvmOverloads constructor(
    var output: String = "",
    var foo: String = ""
) {
    companion object {
        /** Default behavior which will include everything as is. */
        @JvmStatic
        val ALL = Update4jConfiguration()
    }
}