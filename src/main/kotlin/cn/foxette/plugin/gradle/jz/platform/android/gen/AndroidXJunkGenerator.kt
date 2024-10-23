package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import org.gradle.api.Task

internal class AndroidXJunkGenerator(
    dir: String,
    logger: Logger,
    output: String,
    param: AndroidJunkCodeParam
) : AndroidJunkGenerator(dir, logger, output, param)