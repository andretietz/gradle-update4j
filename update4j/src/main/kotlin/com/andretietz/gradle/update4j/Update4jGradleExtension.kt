package com.andretietz.gradle.update4j

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class Update4jGradleExtension(project: Project) {
    var config: NamedDomainObjectContainer<Update4j> = project.container(Update4j::class.java) { Update4j(it) }
}

data class Update4j @JvmOverloads constructor(
    var output: String
)