# gradle-update4j
This is a gradle plugin to generate a bundle directory containing all required
dependencies and an `update.xml` file for the [update4j library](https://github.com/update4j/update4j).
All maven dependencies will be added as such in order to minimize selfhosted files.

FYI: There's no version released yet! Use `0.1.0-SNAPSHOT` to try.
# Usage
### 1. Using Release Versions:
```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "com.andretietz.gradle:update4j:0.1.0"
        ...
    }
}
```
OR
### 1. Using Snapshot Versions:
Snapshots are getting build automatically on github actions for each master commit:
```groovy
buildscript {
    repositories {
        maven { url { "https://oss.sonatype.org/content/repositories/snapshots" }}
        ...
    }
    dependencies {
        classpath "com.andretietz.gradle:update4j:0.1.0-SNAPSHOT"
        ...
    }
}
```
### 2. And the apply:

```groovy
apply plugin: "com.andretietz.gradle.update4j"
update4j {
  // name of the generated config xml. optional, default: update.xml
  configurationFileName = "update.xml"
  // directory in which to find the files. mandatory!
  remoteLocation = "https://andretietz.com/updates/testapp" 
  // launcher class. mandatory!
  launcherClass = "com.andretietz.update4j.Main"
  // use maven links in the update.xml. optional, default: true
  // if false, it copies all dependencies into the output folder
  useMaven = true 
  // directory name in which the bundle should be generated. optional, default: update4j
  bundleLocation = "update4j"
  // name of the directory within the bundle location, in which the resources should end up in. optional, default: .
  resourcesDirectoryName = "."

  // files you want to end up in the bundle directory. optional
  resources = [
    "somefile.txt",
    "lib" // not supported atm
  ]
}
```

## 3. Call
```
gradlew generateBundle
```

# What can it do already?
* Generating an `build/update4j/update.xml`
  * adding: `launcherClass`
  * adding maven dependencies (and it's transitives)
    * xml will contain full maven repository links if found
    * if not found it'll copy it to the target dir: `build/update4j`
  * adding local project dependencies by copying the artifacts into `build/update4j`
  * additional resource files will be copied into `build/update4j/res` and added to xml
  * os attribute support, if you add the dependency
## Not working atm:
  * local file dependencies (such as `implementation fileTree(dir: 'libs', include: '*.jar')` or `implementation files("somelib/samplelib-1.0.0.jar")`)
  * file signing
  * resource files as directory
  * adding anything to the module path (within the config xml)
  
# Development
In order to start developing on this plugin:

1. Go into the [`settings.gradle`](settings.gradle) and uncomment the app include.
2. build the update4j plugin ```gradlew update4j:build```
3. reverse step 1.

The `app` project requires a build `update4j` project in order to run.

## Debugging:
Run the app in terminal:
```gradlew app:clean app:generateBundle -Dorg.gradle.debug=true```

Setup your IDE for remote debugging (e.g. Intellij):
![debugging](https://user-images.githubusercontent.com/2174386/98470418-bb633300-21e5-11eb-8e3c-1ffb87685a93.PNG)


After debugging run ```gradlew --stop``` so that you can restart.

This is a very weird approach and full of effort. If you know a better one, please let me know!


## LICENSE
```
Copyrights 2020 André Tietz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
