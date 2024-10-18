package cn.foxette.plugin.gradle.jz.log

internal class SystemLogger : Logger {

    override fun log(msg: String) {
        println(msg)
    }

    override fun error(msg: String, th: Throwable?) {
        System.err.println(msg)
        th?.printStackTrace()
    }
}