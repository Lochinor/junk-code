package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam

internal class DefaultAndroidJunkGenerator(
    dir: String,
    logger: Logger,
    output: String,
    param: AndroidJunkCodeParam
) : AndroidJunkGenerator(dir, logger, output, param)