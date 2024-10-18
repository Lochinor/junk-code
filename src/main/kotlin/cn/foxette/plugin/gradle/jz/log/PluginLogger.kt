package cn.foxette.plugin.gradle.jz.log

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

internal class PluginLogger(private val provider: () -> Project) : Logger {

    override fun log(msg: String) {
        provider().logger.log(LogLevel.INFO, msg)
    }

    override fun error(msg: String, th: Throwable?) {
        provider().logger.error(msg, th)
    }
}