package cn.foxette.plugin.gradle.jz.platform.jvm.entity

class ClassInfo(
    val pkg: String,
    val name: String,
    val asm: ClassType,
    val fields: List<FieldInfo> = emptyList(),
    val methods: List<MethodInfo> = emptyList()
)