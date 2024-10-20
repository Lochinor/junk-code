package cn.foxette.plugin.gradle.jz.platform.android.task

import cn.foxette.plugin.gradle.jz.configuration.ProjectConst
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeExtension
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import cn.foxette.plugin.gradle.jz.platform.android.gen.AndroidXJunkGenerator
import cn.foxette.plugin.gradle.jz.platform.android.gen.DefaultAndroidJunkGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.Integer.max
import kotlin.math.min

abstract class AndroidJunkCodeTask : DefaultTask() {

    private fun adjustParams(property: Property<Int>, defaultValue: Int = 0): Int {
        val value = property.getOrElse(defaultValue)
        return if (value < 0) defaultValue else value
    }

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(AndroidJunkCodeExtension::class.java) ?: return

        val packageCount = adjustParams(extension.packageCount)
        val maxActivityCount = adjustParams(extension.maxActivityCount)
        // 限制Activity的总数 > 3711 BUG
        val maxPackageActivityCount = min(adjustParams(extension.maxPackageActivityCount), 3200)
        val minPackageActivityCount = adjustParams(extension.minPackageActivityCount)

        val avg = max(maxActivityCount, packageCount * (maxActivityCount + minPackageActivityCount) * 3 / 2)

        val params = AndroidJunkCodeParam(
            extension.fileName.get(),
            extension.packageName.get() + "." + extension.moduleName.get(),
            packageCount,
            maxActivityCount,
            maxPackageActivityCount,
            minPackageActivityCount,
            extension.resPrefix.get(),
            extension.skipResource.get(),
            // 按照一个 activity 5 个字符串 2 个 drawable
            adjustParams(extension.stringsCount, avg * 5),
            adjustParams(extension.drawableCount, avg * 2)
        )

        val buildDir = project.buildDir
        val outputDir = File(buildDir, ProjectConst.JUNK_CODE_DIR_NAME)

        val generator = if (extension.androidxEnable.get())
            AndroidXJunkGenerator(this, buildDir.absolutePath, outputDir.absolutePath, params)
        else
            DefaultAndroidJunkGenerator(this, buildDir.absolutePath, outputDir.absolutePath, params)

        generator.execute()
    }
}