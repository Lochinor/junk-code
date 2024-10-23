package cn.foxette.plugin.gradle.jz.platform.android.ext

import org.gradle.api.provider.Property

interface AndroidJunkCodeExtension {

    /**
     * aar 的文件名
     */
    val fileName: Property<String>

    /**
     * 自动引用
     */
    val autoUsage: Property<Boolean>

    /**
     * 检测到 junk 目录中没有文件时自动生成
     */
    val autoGenerate: Property<Boolean>

    /**
     * aar 的模块名
     */
    val moduleName: Property<String>

    /**
     * 包名
     */
    val packageName: Property<String>

    /**
     * 包数量
     */
    val packageCount: Property<Int>

    /**
     * Activity 数量上限  <= 0 不限制
     */
    val maxActivityCount: Property<Int>

    /**
     * 每个包里面最多 Activity 数量
     */
    val maxPackageActivityCount: Property<Int>

    /**
     * 每个包里面最少 Activity 数量
     */
    val minPackageActivityCount: Property<Int>

    /**
     * 每个包下普通Java类的最小数量
     */
    val minPackageBlurCount: Property<Int>

    /**
     * 每个包下普通Java类的最大数量
     */
    val maxPackageBlurCount: Property<Int>

    /**
     * 资源前缀
     */
    val resPrefix: Property<String>

    /**
     * 跳过资源文件生成，Activity的layout除外
     */
    val skipResource: Property<Boolean>

    /**
     * 字符串的数量
     */
    val stringsCount: Property<Int>

    /**
     * 图片资源的数量
     */
    val drawableCount: Property<Int>

    /**
     * 是否支持 androidX
     */
    val androidxEnable: Property<Boolean>
}