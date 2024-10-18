package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import org.gradle.api.Task

internal class DefaultAndroidJunkGenerator(
    task: Task,
    dir: String,
    output: String,
    param: AndroidJunkCodeParam
) : AndroidJunkGenerator(task, dir, output, param)