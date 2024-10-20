package cn.foxette.plugin.gradle.jz.platform.android

import cn.foxette.plugin.gradle.jz.configuration.ProjectConst
import cn.foxette.plugin.gradle.jz.platform.android.gen.AndroidJunkGenerator
import cn.foxette.plugin.gradle.jz.platform.jvm.JvmJunkCodeProducer
import org.gradle.internal.impldep.org.apache.commons.lang.text.StrBuilder
import kotlin.math.max
import kotlin.random.Random

internal open class AndroidJunkCodeProducer(private val generator: AndroidJunkGenerator) : JvmJunkCodeProducer() {

    companion object {
        // view 的最深层级
        const val MAX_DEPTH = 3
        const val VIEW_ATTRIBUTE_MASK = 1024

        const val ANDROID_SCHEMA = "https://schemas.android.com/apk/res/android"

        val COLORS = "0123456789abcdef".toCharArray()

        val VIEWS_GROUP = listOf(
            "FrameLayout", "LinearLayout", "RelativeLayout",
        )

        val VIEWS = listOf(
            "FrameLayout", "LinearLayout", "RelativeLayout", "GridLayout", "Chronometer", "Button", "ImageButton",
            "ImageView", "ProgressBar", "TextView", "ViewFlipper", "ListView", "GridView", "StackView",
            "android.widget.Button", "Space", "View"
        )

        val DIMENS = listOf("match_parent", "wrap_content", "dp")
        val IMAGE_SCALE_TYPE =
            listOf("matrix", "fitXY", "fitStart", "fitCenter", "fitEnd", "center", "centerCrop", "centerInside")

    }

    open fun randomStringValues(length: Int = 255, unicode: Boolean = false): String {
        return "@null"
    }

    /**
     * 格式 xxx_xxx_xxx
     */
    open fun randomResourceName(prefix: String = "", max: Int = 16): String {
        val len = Random.nextInt(ProjectConst.MIN_NAME_LENGTH, max(max, ProjectConst.MIN_NAME_LENGTH + 1))
        val array = randomName(len)

        // 替换成 _
        val separatorCnt = len / ProjectConst.RESOURCE_SEPARATOR_NUM

        for (i in 0 until separatorCnt) {
            val index = Random.nextInt(
                1 + i * ProjectConst.RESOURCE_SEPARATOR_NUM,
                (1 + i) * ProjectConst.RESOURCE_SEPARATOR_NUM
            )
            array[index] = '_'
        }

        return prefix + array.concatToString()
    }

    fun randomSize(): String {
        val size = DIMENS[Random.nextInt(DIMENS.size)]
        return if (size == "dp") "${Random.nextInt(96)}dp" else size
    }

    private fun viewAttribute(view: String): String {
        val value = randomSeed()
        val attr = StrBuilder()

        val cnd1 = value % 2 == 0
        val cnd2 = value % 3 == 0
        val cnd3 = value % 5 == 0
        val cnd4 = value % 7 == 0
        val cnd5 = value % 11 == 0

        fun attribute(s: String) = attr.append("\n").append(s)

        when (view) {
            "LinearLayout" -> {
                // 生成随机方向
                attribute(if (cnd1) "android:orientation=\"vertical\"" else "android:orientation=\"horizontal\"")
            }

            "Button", "TextView", "android.widget.Button" -> {
                // 展示随机 Text
                if (cnd1)
                    attribute("android:text=\"${randomStringValues()}")
                else if (cnd2)
                    attribute("android:text=\"@string/${randomNext(generator.strings)}\"")

                if (cnd3) attribute("android:textSize=\"${Random.nextInt(12, 32)}dp\"")
                if (cnd4) attribute(" android:textColor=\"${randomColor()}\"")
                if (cnd5) attribute("android:textAllCaps=\"${Random.nextBoolean()}\"")
            }

            "ImageButton", "ImageView" -> {
                if (cnd3) attribute("android:src=\"@drawable/${randomNext(generator.drawables)}\"")
                if (cnd4) attribute("android:scaleType=\"${randomNext(IMAGE_SCALE_TYPE)}\"")
            }

            else -> {}
        }

        // 背景颜色
        if (cnd3 && cnd4) {
            val color =
                if (cnd5) "@drawable/${randomNext(generator.drawables)}" else randomColor()

            attr.append("\n").append("android:background=\"$color\"")
        }

        return attr.toString()
    }

    private fun viewSize(root: Boolean): String {
        return if (root) """
                android:layout_width="match_parent"
                android:layout_height="match_parent"
        """
        else {
            val width = randomSize()
            val height = randomSize()

            """
               android:layout_width="$width"
               android:layout_height="$height"              
            """
        }.trimIndent()
    }

    /**
     * @return 布局头, 尾
     */
    open fun randomViewGroup(id: String? = null, root: Boolean = false, depth: Int = 0): Pair<String, String> {
        val view = VIEWS_GROUP[Random.nextInt(VIEWS_GROUP.size)]

        val tpl = if (root) """
            <?xml version="1.0" encoding="utf-8"?>
            <$view xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                xmlns:tools="http://schemas.android.com/tools"
                ${viewSize(true)}
                ${viewAttribute(view)}>
        """ else """
            <$view
                ${viewSize(true)}
                ${viewAttribute(view)}>
            """

        return tpl.trim() to "</$view>"
    }

    /**
     * @return View 开始 结束
     */
    open fun randomView(
        id: String? = null,
        depth: Int = 1,
    ): Triple<String, String, String> {
        val viewPools = getViewPools()
        val view = viewPools[Random.nextInt(viewPools.size)]
        val content = """
            <$view
                ${viewSize(false)}
                ${viewAttribute(view)}>
            """

        return Triple(view, content, "</$view>")
    }

    fun randomColor(): String {
        val sb = StringBuilder()
        sb.append("#")
        for (i in 0..5) {
            sb.append(COLORS[Random.nextInt(COLORS.size)])
        }
        return sb.toString()
    }

    fun isViewGroup(name: String): Boolean {
        return VIEWS_GROUP.contains(name)
    }

    protected open fun getViewPools(): List<String> = VIEWS

    fun randomSeed(): Int {
        return Random.nextInt(VIEW_ATTRIBUTE_MASK)
    }

    fun <T> randomNext(list: List<T>) = list[Random.nextInt(list.size)]
}