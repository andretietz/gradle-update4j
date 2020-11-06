package com.andretietz.gradle.update4j

import io.kotlintest.shouldNotBe
import io.kotlintest.specs.WordSpec
import org.gradle.testfixtures.ProjectBuilder

class Update4jPluginTest : WordSpec({
  "Using the Plugin ID" should {
    "Apply the Plugin" {
      val project = ProjectBuilder.builder().build()
      project.pluginManager.apply("com.andretietz.gradle.update4j")

      project.plugins.getPlugin(Update4jPlugin::class.java) shouldNotBe null
    }
  }
})
