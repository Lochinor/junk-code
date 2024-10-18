package cn.foxette.plugin.gradle.jz.log

internal interface Logger {

    fun log(msg: String)

    fun error(msg: String, th: Throwable?)
}