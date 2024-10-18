package cn.foxette.plugin.gradle.jz.platform.jvm

import cn.foxette.plugin.gradle.jz.configuration.ProjectConst.MIN_NAME_LENGTH
import cn.foxette.plugin.gradle.jz.configuration.ProjectConst.RESOURCE_SEPARATOR_NUM
import kotlin.math.max
import kotlin.random.Random


private const val ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android"

private val KEYWORDS = arrayOf(
    "boolean", "byte", "char", "short", "int", "long", "float", "double", "String",
    "private", "protected", "public", "abstract",
    "final", "static", "synchronized",
    "extends", "implements",
    "class", "interface",
    "new", "this", "super", "instanceof",
    "try", "catch", "finally", "throw", "throws",
    "package", "import",
    "native", "strictfp", "transient", "volatile", "assert", "null", "goto", "void", "const", "continue",
    "default", "false", "true", "case", "enum", "for", "else", "do", "if", "while",
    "return", "break", "switch"
)

private val CHARACTER = "abcdefghijklmnopqrstuvwxyz".toCharArray()

private fun randomName(length: Int): CharArray {
    val chars = CharArray(length)

    for (i in 0 until length) {
        chars[i] = CHARACTER[Random.nextInt(CHARACTER.size)]
    }

    return chars
}

/**
 * 格式 xxx
 */
fun randomPackageName(max: Int = 10): String {
    val len = Random.nextInt(MIN_NAME_LENGTH, max(max, MIN_NAME_LENGTH + 1))

    val name = randomName(len).concatToString()
    // 排除关键字
    if (KEYWORDS.contains(name)) {
        return randomPackageName()
    }

    return name
}

/**
 * 格式 xxx_xxx_xxx
 */
fun randomResourceName(prefix: String = "", max: Int = 16): String {
    val len = Random.nextInt(MIN_NAME_LENGTH, max(max, MIN_NAME_LENGTH + 1))
    val array = randomName(len)

    // 替换成 _
    val separatorCnt = len % RESOURCE_SEPARATOR_NUM

    for (i in 0 until separatorCnt) {
        val index = Random.nextInt(1 + i * RESOURCE_SEPARATOR_NUM, (1 + i) * RESOURCE_SEPARATOR_NUM)
        array[index] = '_'
    }

    return prefix + array.concatToString()
}