package cn.foxette.plugin.gradle.jz.platform.android.ext

import kotlin.random.Random

internal class AndroidJunkCodeParam(
    val fileName: String,
    val appPackageName: String,
    val packageCount: Int,
    /**
     * Activity 数量上限  <= 0 不限制
     */
    val maxActivityCount: Int,

    /**
     * 每个包里面最少 Activity 数量
     */
    private val minPackageActivityCount: Int,
    /**
     * 每个包里面最多 Activity 数量
     */
    private val maxPackageActivityCount: Int,

    /**
     * 每个包下普通Java类的最小数量
     */
    private val minPackageBlurCount: Int,

    /**
     * 每个包下普通Java类的最大数量
     */
    private val maxPackageBlurCount: Int,

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
    fun getPackageActivityCount(current: Int): Int {
        // 已经达到上限了
        if (maxActivityCount in 0 until current) {
            return 0
        }

        val min = if (minPackageActivityCount < 0) 0 else minPackageActivityCount
        val max = maxPackageActivityCount
        return if (max <= min) min else Random.nextInt(min, max)
    }

    /**
     * @param refer 参考值
     */
    fun getPackageBlurCount(refer: Int): Int {
        val min = if (minPackageBlurCount < 0) refer else minPackageBlurCount
        val max = maxPackageBlurCount
        return if (max <= min) min else Random.nextInt(min, max)
    }
}