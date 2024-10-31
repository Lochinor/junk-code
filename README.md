# Android Junk Code Generator

![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/cn.foxette.plugin.gradle.junk-code)


English | [中文](README_zh-CN.md)


Android Junk Code Generator Plugin

## How to use

Add it to the `build.gradle` file located in the root directory of the project.

Before Gradle 8.0 
```kotlin
buildscript {
    dependencies {
        classpath("cn.foxette.plugin.gradle:junk-code:0.0.2")
    }
}
```


After Gradle 8.0 ```kotlin
```kotlin
plugins {
    id("cn.foxette.plugin.gradle.junk-code") version "0.0.2" apply false
}
```


Add the following to the `build.gradle` file of the `app`

```kotlin
plugins {
    // ... other 
    id("cn.foxette.plugin.gradle.junk-code")
}

androidJunkCode {
  // Required: The total number of activities, up to 3600
  activityClassesCount.set(30)

  // -- The following parameters are optional --

  // The number of packages to generate; [Recommended Setting] Default is 1
  packageCount.set(50)
  // Package name, can be the same as the project's package name; [Recommended Setting] Default is cn.foxette.{random}
  packageName.set("cn.foxette.android")
  // The module name for the aar, different from the current module name; Default is jmp{random}
  moduleName.set("plugin")
  // The number of regular class files, default is activityClassesCount * 2
  blurClassesCount.set(20)

  // The default prefix for resource names starting with "jz_"
  resPrefix.set("jz_")
  // The name of the aar, default is junk_code_{plugin version number}
  fileName.set("junk-1.1")
  // Automatically generate if the app/junk directory is empty; Default is true
  autoGenerate.set(true)
  // The number of strings to generate in strings.xml; Default is activityClassesCount * 5
  stringsCount.set(100)
  // The number of drawables to generate; Default is activityClassesCount * 2
  drawableCount.set(50)
  // Automatically add releaseRuntimeOnly fileTree(dir : "junk", include : ["*.aar"]) to include the aar, set to false if not needed
  autoUsage.set(true)
  // Whether to ignore generating drawable.xml and string.xml; Default is false
  ignoreResource.set(false)
}

```

### Code Generation:
* If the `autoGenerate` is set to `true` after deleting the `junk`, the next run or update of the gradle configuration will automatically generate the code.
* Manual generation: execute the following tasks:
    ```shell
    gradle generateAndroidJunkCode
    ```
### License
```text
BSD 3-Clause License

Copyright (c) 2024, Shihwan

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```