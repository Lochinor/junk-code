# Android Junk Code Generator



[中文](README_zh-CN.md)


Android Junk Code Generator Plugin

## How to use

Add it to the `build.gradle` file located in the root directory of the project.

Before Gradle 8.0 
```kotlin
buildscript {
    dependencies {
        classpath("cn.foxette.plugin.gradle:junk-code:0.0.1")
    }
}
```


After Gradle 8.0 ```kotlin
```kotlin
plugins {
    id("cn.foxette.plugin.gradle.junk-code") version "0.0.1" apply false
}
```


Add the following to the `build.gradle` file of the `app`

```kotlin
plugins {
    // ... other 
    id("cn.foxette.plugin.gradle.junk-code")
}

androidJunkCode {
    // The number of packages to generate
    packageCount.set(50)
    // There can be at most 3200 activities per package.
    maxPackageActivityCount.set(30)

    // The following parameters are optional:
    // The default cn.foxette.`random` package name can be generated, which can be consistent with the project package name
    packageName.set("cn.foxette.android")
    // By default, jmp+`Random` AAR module name that is different from the current module name
    moduleName.set("plugin")
    // How many activities must there be at the minimum in each package
    minPackageActivityCount.set(20)

    // The default prefix for jz_ resource names
    resPrefix.set("jz_")
    // The default name consists of the package name and the plugin version number. Custom AAR name
    fileName.set("junk-1.1")
    // The default value is true. Automatically generates when the app/junk directory is empty.
    autoGenerate.set(true)
    // This defaults to the number of activities, which is related to the number of strings generated in the `strings.xml` file.
    stringsCount.set(100)
    // Default is true - automatically adds the "releaseRuntimeOnly" fileTree(dir: "junk", include: ["*.aar"]) way to import aar files, if you don't need it, you can set it to false
    autoUsage.set(true)
    // Default false - whether to ignore generated drawable strings
    skipResource.set(false)
}
```

### Code Generation:
* If the `autoGenerate` is set to `true` after deleting the `junk`, the next run or update of the gradle configuration will automatically generate the code.
* Manual generation: execute the following tasks:
    ```shell
    gradle generateAndroidJunkCode
    ```