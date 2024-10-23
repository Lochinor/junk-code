# Android Junk Code Generator

[English](README.md)

Android 垃圾代码生成插件

### 使用

在项目根目录中的 `build.gradle` 中添加

gradle 8.0 之前
```kotlin
buildscript {
    dependencies {
        classpath("cn.foxette.plugin.gradle:junk-code:0.0.1")
    }
}
```

gradle 8.0 之后
```kotlin
plugins {
    id("cn.foxette.plugin.gradle.junk-code") version "0.0.1" apply false
}
```

`app` 的 `build.gradle` 中添加如下

```kotlin
plugins {
    // ... other
    id("cn.foxette.plugin.gradle.junk-code")
}


androidJunkCode {
    //  要生成包的数量
    packageCount.set(50)
    // 每个包下最多有多少个activity，最多 3200
    maxPackageActivityCount.set(30)

    // 以下参数可选
    // 默认 cn.foxette.随机生成 包名，可以与项目包名一致
    packageName.set("cn.foxette.android")
    // 默认 jmp+随机生成 aar的模块名，与当前的模块名不同
    moduleName.set("plugin")
    // 每个包下最少有多少个activity
    minPackageActivityCount.set(20)

    // 默认jz_ 资源名称的前缀
    resPrefix.set("jz_")
    // 默认由包名和插件版本号组成 自定义aar的名称
    fileName.set("junk-1.1")
    // 默认true 检测到 app/junk 目录下为空时，自动生成
    autoGenerate.set(true)
    // 默认与 activity 的数量相关 生成在strings.xml中的字符串数量
    stringsCount.set(100)
    // 默认true 自动添加 releaseRuntimeOnly fileTree(dir : "junk", include : ["*.aar"]) 方式引入aar，如不需要可以设置成false
    autoUsage.set(true)
    // 默认false  是否忽略生成 drawable string
    skipResource.set(false)
}
```
