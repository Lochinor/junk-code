# Android Junk Code Generator


### 使用

在项目根目录中的 `build.gradle` 中添加

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
    // 包名，可以与项目包名一致
    packageName.set("cn.foxette.android")
    // 与当前的模块名不同
    moduleName.set("plugin")
    // 自定义aar的名称
    fileName.set("junk-1.0")
    // 检测到 app/junk 目录下为空时，自动生成
    autoGenerate.set(true)
    // 要生成包的数量
    packageCount.set(50)
    // 每个包下有多少个activity
    activityCountPerPackage.set(30)
    // 默认使用 releaseRuntimeOnly fileTree(dir : "junk", include : ["*.aar"]) 方式引入了，如不需要可以设置成false
    autoUsage.set(true)

    // 设置 true 会忽略生成 drawable string
    skipResource.set(false)
}
```
