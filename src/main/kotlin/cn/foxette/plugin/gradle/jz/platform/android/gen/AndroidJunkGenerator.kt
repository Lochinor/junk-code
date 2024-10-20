package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.log.PluginLogger
import cn.foxette.plugin.gradle.jz.platform.android.AndroidJunkCodeProducer
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.ClassInfo
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.ClassType
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.FieldInfo
import org.gradle.api.Task
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileWriter
import java.util.*
import kotlin.random.Random

internal abstract class AndroidJunkGenerator(
    task: Task,
    // 工作目录
    dir: String,
    // 输出保存的目录
    protected val output: String,
    protected val param: AndroidJunkCodeParam
) : Logger by PluginLogger({ task.project }) {

    protected companion object {
        const val MAX_ID_SIZE = 8192
    }

    private val workspace = File(dir, "androidJunkCodeGen")
    protected val classesDirName = "classes"


    /**
     * 用于注册到 Manifest 中
     */
    private val activityNames = ArrayList<String>(param.maxActivityCount)

    private val blurClasses = BlurClasses()

    internal val strings = ArrayList<String>(param.stringsCount)
    internal val drawables = ArrayList<String>(param.drawableCount)
    internal val ids = ArrayList<String>(MAX_ID_SIZE ushr 1)
    internal val layouts = ArrayList<String>(param.maxActivityCount)

    protected open val producer: AndroidJunkCodeProducer get() = AndroidJunkCodeProducer(this)

    protected open fun cleanUp() {
        workspace.deleteRecursively()
        activityNames.clear()
        strings.clear()
    }

    open fun execute() {
        val start = System.nanoTime()
        log("Junk code task execute start.")

        cleanUp()

        generateResource()
        generateClasses()

        val end = System.nanoTime()
        val timeMills = (end - start) / 1_000_000
        val s = timeMills / 1000
        val ms = timeMills % 1000

        log("用时：${s}.${ms} 秒")
    }

    protected open fun generateResource() {
        if (param.skipResource) {
            log("Skip generateResource.")
            return
        }

        log("Start generateResource...")
        strings.addAll(generateResourceIds(param.stringsCount))
        drawables.addAll(generateResourceIds(param.drawableCount))
    }

    /**
     * 生成随机资源名称
     *
     * @return 资源 ids
     */
    protected open fun generateResourceIds(max: Int): List<String> {
        val set = HashSet<String>()
        val prefix = param.resPrefix

        // 随机增加 10%
        val seed = max / 10
        val cnt = max + if (seed > 0) Random.nextInt(seed) else 0

        repeat(cnt) {
            val name = producer.randomResourceName(prefix)
            set.add(name)
        }

        return set.toList()
    }

    protected fun getTypedName(packageName: String?, className: String): String {
        val fullName = if (packageName.isNullOrEmpty()) className else "$packageName/$className"
        return fullName.replace(".", "/")
    }

    protected open fun generateClasses() {
        log("Start generate classes...")
        val count = param.packageCount
        for (i in 0 until count) {
            val packageName = param.appPackageName + "." + producer.randomPackageName()
            // 生成不相关的类
            val blurCount = Random.nextInt(1, 5)
            for (j in 0 until blurCount) {
                generateBlurClasses(packageName)
            }

            val activityCount = param.getPackageActivityCount()
            for (k in 0 until activityCount) {
                // 生成Activity
                generateActivity(packageName)
            }
        }
    }


    /**
     * 生成干扰项 class
     */
    protected open fun generateBlurClasses(packageName: String): ClassInfo {
        val className = producer.randomClassName()
        if (blurClasses.exist(packageName, className)) {
            return generateBlurClasses(packageName)
        }

        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val owner = getTypedName(packageName, className)
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null)

        // 生成字段
        val fields = HashSet<String>(16)
        val cnt = Random.nextInt(2, 16)
        val fieldInfos = ArrayList<FieldInfo>()
        repeat(cnt) {
            val field = producer.randomFieldName()
            if (fields.add(field)) {
                fieldInfos.add(FieldInfo(field, "java.lang.String"))
                cw.visitField(Opcodes.ACC_PUBLIC, field, "Ljava/lang/String;", null, null).visitEnd()
            }
        }

        val descriptor = arrayOf("()V", "()Ljava/lang/String;", "()I")

        // 生成随机方法
        val methods = HashSet<String>(16)
        // 静态方法 0-3
        val sMethodCnt = Random.nextInt(3)
        repeat(sMethodCnt) {
            val name = producer.randomMethodName()
            if (!methods.add(name)) {
                return@repeat
            }

            val index = Random.nextInt(descriptor.size)
            val des = descriptor[index]

            val method = cw.visitMethod(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, name, des, null, null
            )

            method.visitCode()
            when (index) {
                0 -> {
                    // 调用log日志
                    method.visitLdcInsn(className)
                    method.visitLdcInsn(name)
                    method.visitMethodInsn(
                        Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false
                    )
                    method.visitInsn(Opcodes.POP)
                    method.visitInsn(Opcodes.RETURN)
                }

                1 -> {
                    // 返回一个随机字符串
                    method.visitLdcInsn(producer.randomStringValues())
                    method.visitInsn(Opcodes.ARETURN)
                }

                2 -> {
                    // 返回一个 hash
                    method.visitLdcInsn(producer.randomStringValues(unicode = true))
                    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false)
                    method.visitInsn(Opcodes.IRETURN)
                }

                else -> {
                    method.visitInsn(Opcodes.RETURN)
                }
            }

            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        writeClassToFile(packageName, className, cw.toByteArray())

        val asm = ClassType()
        val r = ClassInfo(packageName, className, asm, fieldInfos)
        blurClasses.addClass(r, true)
        return r
    }

    protected open fun generateActivity(packageName: String) {
        val className = producer.randomClassName(7)
        val activityName = className + "Activity"
        if (blurClasses.exist(packageName, activityName)) {
            generateActivity(packageName)
            return
        }
        activityNames.add("$className.$activityName")

        // 生成布局文件
        while (true) {
            val layoutName = param.resPrefix + "layout_" + className.lowercase()
            if (layouts.contains(layoutName)) {
                continue
            }

            generateLayout(layoutName)
            break
        }

        // 保存当前类里面的所有方法名，防止重名
        val methods = hashSetOf<String>()
        methods.add("onCreate")

        val self = getTypedName(packageName, activityName)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, self, null, "android/app/Activity", null)

        val ccm = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        ccm.visitVarInsn(Opcodes.ALOAD, 0)
        ccm.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Activity", "<init>", "()V", false)
        ccm.visitInsn(Opcodes.RETURN)
        ccm.visitMaxs(1, 1)
        ccm.visitEnd()

        // 生成onCreate 方法
        val mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "onCreate", "(Landroid/os/Bundle;)V", null, null)
        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitVarInsn(Opcodes.ALOAD, 1)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/app/Activity", "onCreate", "(Landroid/os/Bundle;)V", false)

        // new 一个对象，并给其字段赋值
        val other = blurClasses.randomBlur()
        val otherType = getTypedName(other.pkg, other.name)
        mv.visitTypeInsn(Opcodes.NEW, otherType)
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, otherType, "<init>", "()V", false)

        mv.visitVarInsn(Opcodes.ASTORE, 2)
        mv.visitVarInsn(Opcodes.ALOAD, 2)

        other.fields.forEach { field ->
            mv.visitVarInsn(Opcodes.ALOAD, 2)
            mv.visitLdcInsn(producer.randomStringValues())
            mv.visitFieldInsn(Opcodes.PUTFIELD, otherType, field.name, "Ljava/lang/String;")
        }

        val bytes = cw.toByteArray()
        writeClassToFile(packageName, activityName, bytes)
    }

    protected fun generateLayout(layoutName: String): List<String> {

        val ids = mutableSetOf<String>()
        val childs = Random.nextInt(2, 8)
        // 生成布局

        val xml = StringBuilder()

        val (root, end) = producer.randomViewGroup(root = true)
        xml.append(root)
        repeat(childs) {
            val seed = producer.randomSeed()
            var depth = 0
            val stack = Stack<String>()

            do {
                val viewId = getResourceId()
                val hasId = ids.add(viewId)

                val (type, view, suffix) = producer.randomView(if (hasId) viewId else null, depth)

                xml.append(view)
                stack.add(suffix)
                depth++
            } while (depth < AndroidJunkCodeProducer.MAX_DEPTH && producer.isViewGroup(type) && seed > 256)

            while (!stack.empty()) {
                xml.append(stack.pop())
            }
        }

        xml.append(end)

        val file = File(workspace, "res/layout/$layoutName.xml")
        writeStringToFile(file, xml.toString())
        return ids.toList()
    }

    protected open fun useRandomId(exists: Collection<*>) = exists.size < MAX_ID_SIZE

    protected open fun getResourceId(): String {
        if (useRandomId(this.ids)) {
            val id = producer.randomResourceName()
            if (ids.contains(id)) {
                return getResourceId()
            }

            ids.add(id)
            return id
        }
        return this.ids[Random.nextInt(this.ids.size)]
    }

    protected fun writeClassToFile(packageName: String, className: String, bytes: ByteArray) {
        val dir = File(workspace, classesDirName)
        val parent = File(dir, packageName.replace(".", "/"))
        val file = File(parent, "$className.class")

        if (!parent.exists()) {
            parent.mkdirs()
        }

        if (file.exists()) {
            file.createNewFile()
        }

        file.writeBytes(bytes)
    }

    protected fun writeStringToFile(file: File, content: String): Boolean {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        return FileWriter(file).runCatching {
            use { write(content) }
        }.isSuccess
    }

}