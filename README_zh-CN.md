# Android Junk Code Generator
![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/cn.foxette.plugin.gradle.junk-code)


[English](README.md) | 中文

Android 垃圾代码生成插件

### 使用

在项目根目录中的 `build.gradle` 中添加

gradle 8.0 之前
```kotlin
buildscript {
    dependencies {
        classpath("cn.foxette.plugin.gradle:junk-code:0.0.2")
    }
}
```

gradle 8.0 之后
```kotlin
plugins {
    id("cn.foxette.plugin.gradle.junk-code") version "0.0.2" apply false
}
```

`app` 的 `build.gradle` 中添加如下

```kotlin
plugins {
    // ... other
    id("cn.foxette.plugin.gradle.junk-code")
}

androidJunkCode {
  // 必填：activity的总数，最多 3600
  activityClassesCount.set(30)

  // -- 以下参数可选 --

  // 要生成包的数量；[建议设置] 默认1
  packageCount.set(50)
  // 包名，可以与项目包名一致；[建议设置] 默认 cn.foxette.{随机}
  packageName.set("cn.foxette.android")
  // aar的模块名，与当前的模块名不同；默认 jmp{随机}
  moduleName.set("plugin")
  // 普通 class 数量，默认 activityClassesCount * 2
  blurClassesCount.set(20)

  // 默认jz_ 资源名称的前缀
  resPrefix.set("jz_")
  // aar的名称，默认 junk_code_{插件版本号}
  fileName.set("junk-1.1")
  // 检测到 app/junk 目录下为空时，自动生成；默认true
  autoGenerate.set(true)
  // 生成在strings.xml中的字符串数量；默认 activityClassesCount * 5
  stringsCount.set(100)
  // 生成 drawable 数量；默认 activityClassesCount * 2
  drawableCount.set(50)
  // 自动添加 releaseRuntimeOnly fileTree(dir : "junk", include : ["*.aar"]) 方式引入aar，如不需要可以设置成false
  autoUsage.set(true)
  // 是否忽略生成 drawable.xml string.xml; 默认false
  ignoreResource.set(false)
}
```

### 代码生成
* 如果`autoGenerate`设置为`true`时，删除`junk`后，下次运行或者更新同步gradle配置会自动生成
* 手动生成：执行以下任务
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