package cn.foxette.plugin.gradle.jz.platform.android.task

import cn.foxette.plugin.gradle.jz.configuration.ProjectConst
import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.log.SystemLogger
import cn.foxette.plugin.gradle.jz.platform.android.AndroidJunkCode
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import cn.foxette.plugin.gradle.jz.platform.android.gen.AndroidXJunkGenerator
import cn.foxette.plugin.gradle.jz.platform.android.gen.DefaultAndroidJunkGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.math.min
import kotlin.reflect.KProperty0

abstract class AndroidJunkCodeTask : DefaultTask(), Logger by SystemLogger("[$TASK_NAME]") {

    companion object {
        const val TASK_NAME = "AndroidJunkCodeTask"
        const val MAX_ACTIVITY_COUNT = 3600

        /**
         * 默认普通类是 activity 类的倍速数
         */
        const val MULTIPLE_BLUR_TO_ACTIVITY = 3
        const val MULTIPLE_STRING_TO_ACTIVITY = 5
        const val MULTIPLE_DRAWABLE_TO_ACTIVITY = 2
    }

    private fun adjustParams(property: Property<Int>, defaultValue: Int = -1): Int {
        val value = property.getOrElse(defaultValue)
        return if (value < 0) defaultValue else value
    }

    private fun requireParam(kp0: KProperty0<Property<Int>>): Int {
        val property = kp0.get()
        val name = kp0.name
        return property.orNull ?: throw GradleScriptException(
            "Please add `$name` in `${AndroidJunkCode.EXTENSION_NAME}`.",
            NullPointerException("$name is null")
        )
    }

    @TaskAction
    fun execute() {
        log("Prepare execute android junk generator")
        val extension = AndroidJunkCode.extension ?: return

        val packageCount = adjustParams(extension.packageCount, 1)
        // 限制Activity的总数 > 3711 BUG
        val activityClassesCount = min(requireParam(extension::activityClassesCount), MAX_ACTIVITY_COUNT)
        val blurClassesCount =
            adjustParams(extension.blurClassesCount, activityClassesCount * MULTIPLE_BLUR_TO_ACTIVITY)
        val drawableCount = adjustParams(extension.drawableCount, activityClassesCount * MULTIPLE_DRAWABLE_TO_ACTIVITY)
        val stringsCount = adjustParams(extension.stringsCount, activityClassesCount * MULTIPLE_STRING_TO_ACTIVITY)

        val params = AndroidJunkCodeParam(
            fileName = extension.fileName.get(),
            appPackageName = extension.packageName.get() + "." + extension.moduleName.get(),
            packageCount = packageCount,
            activityClassesCount = activityClassesCount,
            stringsCount = stringsCount,
            blurClassesCount = blurClassesCount,
            skipResource = extension.ignoreResource.get(),
            drawableCount = drawableCount,
            resPrefix = extension.resPrefix.get()
        )

        val buildDir = AndroidJunkCode.buildDir
        val outputDir = File(AndroidJunkCode.projectDir, ProjectConst.JUNK_CODE_DIR_NAME)

        val generator = if (extension.androidxEnable.get())
            AndroidXJunkGenerator(buildDir.absolutePath, this, outputDir.absolutePath, params)
        else
            DefaultAndroidJunkGenerator(buildDir.absolutePath, this, outputDir.absolutePath, params)

        generator.execute()
    }


}