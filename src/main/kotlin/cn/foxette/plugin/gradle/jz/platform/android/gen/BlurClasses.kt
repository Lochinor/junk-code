package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.platform.jvm.entity.ClassInfo
import kotlin.random.Random

class BlurClasses {
    private val classes = ArrayList<ClassInfo>()
    private val classFullNames = HashSet<String>()
    private val blurClasses = ArrayList<ClassInfo>()

    fun exist(pkg: String, name: String) = classFullNames.contains(pkg + name)

    fun addClass(info: ClassInfo, blur: Boolean): Boolean {
        val path = info.pkg + info.name
        if (classFullNames.add(path)) {
            if (blur) blurClasses.add(info)
            return classes.add(info)
        }
        return false
    }

    fun randomBlur(): ClassInfo? {
        return if (blurClasses.isEmpty()) null else blurClasses[Random.nextInt(blurClasses.size)]
    }

    fun blurClassesSize() = blurClasses.size
}