package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.log.PluginLogger
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import cn.foxette.plugin.gradle.jz.platform.jvm.randomPackageName
import cn.foxette.plugin.gradle.jz.platform.jvm.randomResourceName
import org.gradle.api.Task
import java.io.File
import kotlin.random.Random

internal abstract class AndroidJunkGenerator(
    task: Task,
    // 工作目录
    dir: String,
    // 输出保存的目录
    protected val output: String,
    protected val param: AndroidJunkCodeParam
) : Logger by PluginLogger({ task.project }) {

    private val workspace = File(dir, "androidJunkCodeGen")
    protected val classesDirName = "classes"


    /**
     * 用于注册到 Manifest 中
     */
    private val activityNames = ArrayList<String>(param.maxActivityCount)
    private val stringIds = ArrayList<String>(param.stringsCount)
    private val drawableIds = ArrayList<String>(param.drawableCount)

    protected open fun cleanUp() {
        workspace.deleteRecursively()
        activityNames.clear()
        stringIds.clear()
    }

    open fun execute() {
        val start = System.nanoTime()
        log("Junk code task execute start.")

        cleanUp()

        generateResource()
        generateClasses()

        val end = System.nanoTime()
        val timeMills = (end - start) / 1_000_000
        val s = timeMills / 1000
        val ms = timeMills % 1000

        log("用时：${s}.${ms} 秒")
    }

    protected open fun generateResource() {
        if (param.skipResource) {
            log("Skip generateResource.")
            return
        }

        log("Start generateResource...")
        stringIds.addAll(generateResourceIds(param.stringsCount))
        drawableIds.addAll(generateResourceIds(param.drawableCount))
    }

    /**
     * 生成随机资源名称
     *
     * @return 资源 ids
     */
    protected open fun generateResourceIds(max: Int): List<String> {
        val set = HashSet<String>()
        val prefix = param.resPrefix

        // 随机增加 10%
        val seed = max / 10
        val cnt = max + if (seed > 0) Random.nextInt(seed) else 0

        repeat(cnt) {
            val name = randomResourceName(prefix)
            set.add(name)
        }

        return set.toList()
    }

    protected open fun generateClasses() {
        log("Start generate classes...")
        val count = param.packageCount
        for (i in 0 until count) {
            val packageName = randomPackageName()
            // 生成不相关的类
            generateBlurClasses()

            val activityCount = param.getPackageActivityCount()
            for (j in 0 until activityCount) {
                // TODO 生成Activity
            }
        }
    }

    protected open fun generateBlurClasses() {

    }

    protected fun randomStringValue(): String {
        return ""
    }
}