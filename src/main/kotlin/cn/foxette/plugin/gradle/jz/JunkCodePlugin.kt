package cn.foxette.plugin.gradle.jz

import cn.foxette.plugin.gradle.jz.platform.android.AndroidJunkCode
import org.gradle.api.Plugin
import org.gradle.api.Project

class JunkCodePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        AndroidJunkCode().apply(project)
    }

    private fun isAndroidEnvironment(project: Project): Boolean {
        val plugins = project.plugins
        return plugins.hasPlugin("com.android.application")
                || plugins.hasPlugin("com.android.library")
    }

}