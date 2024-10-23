package cn.foxette.plugin.gradle.jz.platform.android

import cn.foxette.plugin.gradle.jz.JunkCodePlatform
import cn.foxette.plugin.gradle.jz.configuration.ProjectConst
import cn.foxette.plugin.gradle.jz.configuration.ProjectConst.JUNK_CODE_DIR_NAME
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeExtension
import cn.foxette.plugin.gradle.jz.platform.android.task.AndroidJunkCodeTask
import cn.foxette.plugin.gradle.jz.platform.jvm.JvmJunkCodeProducer
import org.gradle.api.Project
import java.io.File

object AndroidJunkCode : JunkCodePlatform() {

    private const val EXTENSION_NAME = "androidJunkCode"
    private const val TASK_NAME = "generateAndroidJunkCode"

    private val producer = JvmJunkCodeProducer()

    private lateinit var project: Project
    private lateinit var ext: AndroidJunkCodeExtension

    override fun apply(project: Project) {
        this.project = project
        val extension = AndroidJunkCode.project.extensions.create(EXTENSION_NAME, AndroidJunkCodeExtension::class.java)
        this.ext = extension
        convention(extension)
        createTask(project)
        afterEvaluate(project, extension)
    }

    val buildDir: File get() = project.buildDir
    val projectDir: File get() = project.projectDir
    val extension: AndroidJunkCodeExtension? get() = ext

    private fun convention(extension: AndroidJunkCodeExtension) {
        with(extension) {
            fileName.convention("junk_code_${ProjectConst.VERSION}")
            autoUsage.convention(true)
            autoGenerate.convention(true)
            moduleName.convention("jmp${producer.randomPackageName(5)}")
            // 使用随机包名
            packageName.convention("cn.foxette.${producer.randomPackageName()}")
            packageCount.convention(8)
            stringsCount.convention(-1)
            drawableCount.convention(-1)
            maxActivityCount.convention(-1)
            maxPackageActivityCount.convention(-1)
            minPackageActivityCount.convention(1)
            minPackageBlurCount.convention(-1)
            maxPackageBlurCount.convention(-1)
            resPrefix.convention("jz_")
            skipResource.convention(false)
            androidxEnable.convention(true)
        }
    }

    private fun createTask(project: Project) {
        project.tasks.create(TASK_NAME, AndroidJunkCodeTask::class.java)
    }

    private fun afterEvaluate(project: Project, extension: AndroidJunkCodeExtension) {
        project.afterEvaluate {
            if (extension.autoUsage.get()) {
                project.dependencies.add(
                    "releaseRuntimeOnly",
                    project.fileTree(mapOf("dir" to JUNK_CODE_DIR_NAME, "include" to listOf("*.jar", "*.aar")))
                )
            }

            val autoGenerate = extension.autoGenerate.get()
            if (autoGenerate && notExistsJunkCodeFiles(project)) {
                (project.tasks.findByName(TASK_NAME) as? AndroidJunkCodeTask)?.execute()
            }
        }
    }

    private fun notExistsJunkCodeFiles(project: Project): Boolean {
        return File(project.projectDir, JUNK_CODE_DIR_NAME)
            .listFiles { _, name -> name.endsWith(".aar") }
            .isNullOrEmpty()
    }

}