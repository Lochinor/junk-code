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
     * 每个包里面最多 Activity 数量
     */
    private val maxPackageActivityCount: Int,

    /**
     * 每个包里面最少 Activity 数量
     */
    private val minPackageActivityCount: Int,

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

    fun getPackageActivityCount(): Int {
        val min = if (minPackageActivityCount < 0) 0 else minPackageActivityCount
        val max = if (maxPackageActivityCount < min) min else maxPackageActivityCount
        return Random.nextInt(min, max)
    }
}