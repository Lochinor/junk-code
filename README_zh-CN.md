# Android Junk Code Generator

[English](README.md)

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