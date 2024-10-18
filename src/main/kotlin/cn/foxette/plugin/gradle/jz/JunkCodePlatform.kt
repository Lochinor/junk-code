package cn.foxette.plugin.gradle.jz

import org.gradle.api.Project

abstract class JunkCodePlatform {

    abstract fun apply(project: Project)
}