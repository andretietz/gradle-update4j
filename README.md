Snapshots are getting build automatically on github actions for each master commit:

```groovy
buildscript {
    repositories {
        maven { url { "https://oss.sonatype.org/content/repositories/snapshots" }}
        ...
    }
    dependencies {
        classpath "com.andretietz.gradle:update4j:1.0.0-SNAPSHOT"
        ...
    }
}
```


And the apply:

```groovy
apply plugin: "com.andretietz.gradle.update4j"
update4j {
    // directory where to find the files remotely
    remoteLocation = "file:///Users/andre/repos/avuploader/app/build/update4j"
    launcherClass = "com.andretietz.avuploader.Application"
    // (optional) where the bundle directory will be located after gen.
    bundleLocation = "build/update4j" 
//    resources = [
//            "some/file"
//    ]

}
```


Debug:
Run the app in terminal:
```gradlew app:clean app:generateBundle -Dorg.gradle.debug=true```
```gradlew --stop```

Create a new "Remote" run config using the module `gradle-update4j.update4j.main`
