package cn.foxette.plugin.gradle.jz.platform.jvm

import cn.foxette.plugin.gradle.jz.JunkCodeProducer
import cn.foxette.plugin.gradle.jz.configuration.ProjectConst
import kotlin.math.max
import kotlin.random.Random

open class JvmJunkCodeProducer : JunkCodeProducer {

    protected companion object {

        val KEYWORDS = arrayOf(
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

        val CHARACTER = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val CHARACTER_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
        val CHARACTER_NUMBER = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    }


    protected open fun randomName(length: Int): CharArray {
        val chars = CharArray(length)

        for (i in 0 until length) {
            chars[i] = CHARACTER[Random.nextInt(CHARACTER.size)]
        }

        return chars
    }

    /**
     * 格式 xxx
     */
    open fun randomPackageName(max: Int = 10): String {
        val len = Random.nextInt(ProjectConst.MIN_NAME_LENGTH, max(max, ProjectConst.MIN_NAME_LENGTH + 1))

        val name = randomName(len).concatToString()
        // 排除关键字
        if (KEYWORDS.contains(name)) {
            return randomPackageName()
        }

        return name
    }

    open fun randomClassName(max: Int = 12): String {
        return CHARACTER_UPPERCASE[Random.nextInt(CHARACTER_UPPERCASE.size)] + randomPackageName(max)
    }

    open fun randomFieldName(max: Int = 8): String {
        return randomPackageName(max)
    }

    open fun randomMethodName(max: Int = 7): String {
        return randomPackageName(max)
    }
}