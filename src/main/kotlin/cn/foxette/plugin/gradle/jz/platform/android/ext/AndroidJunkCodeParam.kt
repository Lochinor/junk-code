package cn.foxette.plugin.gradle.jz.platform.android.ext

import kotlin.random.Random

internal class AndroidJunkCodeParam(
    val fileName: String,
    val appPackageName: String,
    val packageCount: Int,
    /**
     * Activity 数量上限  <= 0 不限制
     */
    val activityClassesCount: Int,

    /**
     * 普通Java类的数量
     */
    val blurClassesCount: Int,

    /**
     * 资源前缀
     */
    val resPrefix: String,

    /**
     * 跳过资源文件生成，Activity的layout除外
     */
    val skipResource: Boolean,

    /**
     * 字符串数量
     */
    val stringsCount: Int,

    val drawableCount: Int
) {

    /**
     * @param current 当前数量
     */
    fun randomPackageActivityCount(offset: Int, current: Int, last: Boolean): Int {
        return nextClassesCount(activityClassesCount, offset, current, last)
    }

    /**
     * @param current 当前数量
     */
    fun randomPackageBlurCount(offset: Int, current: Int, last: Boolean): Int {
        return nextClassesCount(blurClassesCount, offset, current, last)
    }

    private fun nextClassesCount(target: Int, offset: Int, current: Int, last: Boolean): Int {
        val max = target - offset
        // 已经达到上限了
        if (max <= current) return 0
        if (last) return max - current
        return Random.nextInt(max - current) + 1
    }
}