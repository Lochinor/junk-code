package cn.foxette.plugin.gradle.jz.log

internal class SystemLogger(private val prefix: String) : Logger {

    companion object {
        const val SEPARATOR = " "
        const val PREFIX_TABLE = "  "
    }

    private fun format(msg: String) = PREFIX_TABLE + prefix + SEPARATOR + msg

    override fun log(msg: String) {
        println(format(msg))
    }

    override fun error(msg: String, th: Throwable?) {
        System.err.println(format(msg))
        th?.printStackTrace()
    }
}