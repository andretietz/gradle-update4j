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
  // directory in which to find the files
  remoteLocation = "https://andretietz.com/updates/testapp" 
  launcherClass = "com.andretietz.update4j.Main"
  resources = [
    "somefile.txt",
    "lib"
  ]
}
```

## 3. Call
```
gradlew generateBundle
```

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
