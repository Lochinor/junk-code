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

abstract class AndroidJunkCodeTask : DefaultTask() {

    private fun adjustParams(property: Property<Int>, defaultValue: Int = 0): Int {
        val value = property.get()
        return if (value < 0) defaultValue else value
    }

    @TaskAction
    fun execute() {
        val extension = project.extensions.findByType(AndroidJunkCodeExtension::class.java) ?: return
        val params = AndroidJunkCodeParam(
            extension.fileName.get(),
            extension.packageName.get() + "." + extension.moduleName.get(),
            adjustParams(extension.packageCount),
            adjustParams(extension.maxActivityCount),
            adjustParams(extension.maxPackageActivityCount),
            adjustParams(extension.minPackageActivityCount),
            extension.resPrefix.get(),
            extension.skipResource.get(),
            adjustParams(extension.stringsCount),
            adjustParams(extension.drawableCount)
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