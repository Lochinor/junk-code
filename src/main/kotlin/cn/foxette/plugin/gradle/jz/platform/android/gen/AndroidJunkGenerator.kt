package cn.foxette.plugin.gradle.jz.platform.android.gen

import cn.foxette.plugin.gradle.jz.log.Logger
import cn.foxette.plugin.gradle.jz.log.PluginLogger
import cn.foxette.plugin.gradle.jz.platform.android.AndroidJunkCodeProducer
import cn.foxette.plugin.gradle.jz.platform.android.AndroidJunkCodeProducer.Companion.ANDROID_SCHEMA
import cn.foxette.plugin.gradle.jz.platform.android.ext.AndroidJunkCodeParam
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.ClassInfo
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.ClassType
import cn.foxette.plugin.gradle.jz.platform.jvm.entity.FieldInfo
import org.gradle.api.Task
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random

internal abstract class AndroidJunkGenerator(
    task: Task,
    // 工作目录
    dir: String,
    // 输出保存的目录
    protected val output: String, protected val param: AndroidJunkCodeParam
) : Logger by PluginLogger({ task.project }) {

    protected companion object {
        const val MAX_ID_SIZE = 8192
        const val ON_CREATE_METHOD_OFFSET = 1
    }

    private val workspace = File(dir, "androidJunkCodeGen")
    protected val classesDirName = "classes"
    private val rClassType = getTypedName(param.appPackageName, "R")

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
        generateManifest()
        writeResourceContent()
        generateKeepProguard()
        writeRFile()

        log("资源文件生成完成，开始打包...")

        // 正在打包
        val outPath = assembleAar()
        val msg = "垃圾代码生成完成：\n\r\t$outPath \n\r\t${fileSize(outPath.length())}"
        log(msg)
        println(msg)

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
        val layoutResName: String
        while (true) {
            val layoutName = param.resPrefix + "layout_" + className.lowercase()
            if (layouts.contains(layoutName)) {
                continue
            }
            layoutResName = layoutName
            break
        }
        val viewIds = generateLayout(layoutResName)

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

        val fields = other.fields
        fields.shuffled()
        val callCnt = if (fields.isEmpty()) 0 else Random.nextInt(fields.size)
        repeat(callCnt) { index ->
            val field = fields[index]
            mv.visitVarInsn(Opcodes.ALOAD, 2)
            mv.visitLdcInsn(producer.randomStringValues(unicode = true))
            mv.visitFieldInsn(Opcodes.PUTFIELD, otherType, field.name, "Ljava/lang/String;")
        }

        // setContentView
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitFieldInsn(Opcodes.GETSTATIC, "$rClassType\$layout", layoutResName, "I")
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, self, "setContentView", "(I)V", false)

        // 随机一些View添加点击事件
        viewIds.shuffled()
        val clickCnt = if (viewIds.isEmpty()) 0 else Random.nextInt(viewIds.size)
        val vl = arrayOf("android/view/View\$OnClickListener")
        repeat(clickCnt) { index ->
            val listener = createOnClickListener(packageName, activityName, index, vl)
            val name = producer.randomMethodName()
            if (!methods.add(name)) {
                return@repeat
            }

            // 一个方法初始化一个view 方便生成
            val method = cw.visitMethod(Opcodes.ACC_PRIVATE, name, "()V", null, null)
            method.visitVarInsn(Opcodes.ALOAD, 0)
//            // R.id.xx
            method.visitFieldInsn(Opcodes.GETSTATIC, "$rClassType\$id", viewIds[index], "I")

            method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, self, "findViewById", "(I)Landroid/view/View;", false)
            method.visitTypeInsn(Opcodes.NEW, listener)
            method.visitInsn(Opcodes.DUP)

            method.visitMethodInsn(
                Opcodes.INVOKESPECIAL, listener, "<init>", "()V", false
            )
//            // invokevirtual android/view/View setOnClickListener (Landroid/view/View$OnClickListener;)V
            method.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "android/view/View",
                "setOnClickListener",
                "(Landroid/view/View\$OnClickListener;)V",
                false
            )

            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        // 生成一些无关的方法
        val list = ArrayList(methods)
        val cnt = Random.nextInt(2, 15)
        repeat(cnt) {
            val s = producer.randomMethodName()
            if (!methods.add(s)) return@repeat
            list.add(s)

            val method = cw.visitMethod(Opcodes.ACC_PRIVATE, s, "()V", null, null)
            method.visitVarInsn(Opcodes.ALOAD, 0)

            // call other method
            val start = ON_CREATE_METHOD_OFFSET
            val end = list.size - 1
            if (start < end) {
                val fn = Random.nextInt(start, end)
                val cnd1 = fn % 2 == 0
                val cnd2 = fn % 3 == 0

                if (cnd1) {
                    // 弹Toast
                    method.visitLdcInsn(producer.randomStringValues(unicode = true))
                    method.visitInsn(Opcodes.ICONST_0)
                    method.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "android/widget/Toast",
                        "makeText",
                        "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;",
                        false
                    )

                    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/widget/Toast", "show", "()V", false)
                    method.visitInsn(Opcodes.RETURN)
                } else {
                    val get = list[fn]
                    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, self, get, "()V", false)
                }
            }

            method.visitInsn(Opcodes.RETURN)
            method.visitMaxs(1, 1)
            method.visitEnd()
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd()

        val bytes = cw.toByteArray()
        writeClassToFile(packageName, activityName, bytes)
    }

    private fun createOnClickListener(
        packageName: String,
        activityName: String,
        index: Int,
        interfaces: Array<String>
    ): String {
        val name = "$activityName\$L$index"
        val ln = getTypedName(packageName, name)
        val cwl = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        cwl.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, ln, null, "java/lang/Object", interfaces)
        val init = cwl.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode()
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();

        // 创建onClick方法
        val onClick = cwl.visitMethod(Opcodes.ACC_PUBLIC, "onClick", "(Landroid/view/View;)V", null, null)
        onClick.visitCode()

        val opt = producer.randomSeed()
        val cnd1 = opt % 2 == 0
        val cnd2 = opt % 3 == 0
        val cnd3 = opt % 5 == 0

        if (cnd1) {
            onClick.visitIntInsn(Opcodes.ALOAD, 1)
            when (Random.nextInt(3)) {
                0 -> onClick.visitInsn(Opcodes.ICONST_0)
                1 -> onClick.visitInsn(Opcodes.ICONST_4)
                else -> onClick.visitIntInsn(Opcodes.BIPUSH, 8)
            }
            onClick.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "setVisibility", "(I)V", true)
        }

        if (cnd2) {
            // 调用log日志
            onClick.visitLdcInsn(name)
            onClick.visitLdcInsn("onClick")
            onClick.visitMethodInsn(
                Opcodes.INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false
            )
            onClick.visitInsn(Opcodes.POP)
        }

        if (cnd3) {
            // Toast
            //     .line 18
            //    invoke-virtual {p0}, Landroid/view/View;->getContext()Landroid/content/Context;
            //
            //    move-result-object v0
            //
            //    const-string v1, "23333"
            //
            //    check-cast v1, Ljava/lang/CharSequence;
            //
            //    const/4 v2, 0x0
            //
            //    invoke-static {v0, v1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
            //
            //    move-result-object v0
            //
            //    invoke-virtual {v0}, Landroid/widget/Toast;->show()V
            //
            //    .line 19
            //    return-void
        }

        onClick.visitInsn(Opcodes.RETURN)
        onClick.visitMaxs(1, 1)
        onClick.visitEnd()

        cwl.visitEnd()
        writeClassToFile(packageName, name, cwl.toByteArray())
        return ln
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

    private fun generateManifest() {
        val manifestFile = File(workspace, "AndroidManifest.xml")

        val activities = activityNames.joinToString("\n\n") { activity -> "<activity android:name=\"$activity\" />" }

        val xml = """<manifest
            ${AndroidJunkCodeProducer.ANDROID_SCHEMA}
            ${AndroidJunkCodeProducer.ANDROID_TOOLS_SCHEMA}
            package="${param.appPackageName}">
                <application>
                    $activities
                </application>
            </manifest>""".trimIndent()
        writeStringToFile(manifestFile, xml)
    }

    private fun writeResourceContent() {
        val file = File(workspace, "res/values/strings.xml")
        val parent = file.parentFile
        if (!parent.exists()){
            parent.mkdirs()
        }

        FileWriter(file)
            .use { writer ->
                writer.write(AndroidJunkCodeProducer.XML_SCHEMA)
                writer.write("\n<resources>")

                strings.forEach { key ->
                    val s = "\n\t<string name=\"$key\">${producer.randomStringValues(64)}</string>"
                    writer.write(s)
                }

                writer.write("\n</resources>")
            }

        drawables.forEach { name ->
            val with = Random.nextInt(96)
            val height = Random.nextInt(96)

            val content = StringBuilder(
                """<vector xmlns:android="http://schemas.android.com/apk/res/android"
                    android:width="${with}dp"
                    android:height="${height}dp"
                    android:viewportWidth="96"
                    android:viewportHeight="96">
                    <path  android:fillColor="${producer.randomColor()}" android:pathData="M"""
            )
            val t: Int = Random.nextInt(3, 40)
            for (i in 0 until t) {
                val fn = Random.nextInt(96)
                content.append(fn).append(",")
            }
            content.append(Random.nextInt(96))
            content.append("z\" />\n").append("</vector>\n").append("\n")

            val drawableFile = File(workspace, "res/drawable/$name.xml")
            writeStringToFile(drawableFile, content.toString())
        }

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

    private fun generateKeepProguard() {

        // 生成混淆保持文件
        val proguard = "-keep class ${param.appPackageName}.**{*;}"
        writeStringToFile(File(workspace, "consumer-rules.pro"), proguard)

        val prefix: String = param.resPrefix

        if (prefix.isEmpty()) {
            return
        }

        val keep = "@layout/$prefix*,@drawable/$prefix*,@string/$prefix*"

        val content = """<?xml version="1.0" encoding="utf-8"?>
        <resources xmlns:tools="http://schemas.android.com/tools"
        tools:keep="$keep"
        tools:shrinkMode="strict"/>""".trimIndent()
        val rnd = producer.randomResourceName()
        writeStringToFile(File(workspace, "res/raw/" + prefix + rnd + "_keep.xml"), content)
    }

    private fun writeRFile() {
        val ss = strings.joinToString("\n") { "int string $it 0x0" }
        val sl = layouts.joinToString("\n") { "int layout $it 0x0" }
        val sd = drawables.joinToString("\n") { "int drawable $it 0x0" }
        val si = ids.joinToString("\n") { "int id $it 0x0" }

        val txt = ss + "\n" + sl + "\n" + sd + "\n" + si
        writeStringToFile(File(workspace, "R.txt"), txt)
    }

    protected fun writeStringToFile(file: File, content: String): Boolean {
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        return FileWriter(file).runCatching {
            use { write(content) }
        }.isSuccess
    }


    private fun assembleAar(): File {
        // 将 class 打包成jar
        val classJar = File(workspace, "classes.jar")
        val dir = File(workspace, classesDirName)

        classJar.outputStream().use { fos ->
            val jos = JarOutputStream(fos)

            dir.listFiles()?.forEach { file ->
                addFileToJar(file, "", jos)
            }

            jos.finish()
        }

        // 删除 class 目录
        // dir.deleteRecursively()

        // 打包aar
        val out = File(output, "${param.fileName}.aar")
        val parent = out.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        } else if (out.exists()) {
            out.delete()
        }

        out.outputStream().use { fos ->
            val zos = ZipOutputStream(fos)
            workspace.listFiles { _, name -> name != classesDirName }?.forEach { file -> addFileToZip(file, "", zos) }
            zos.finish()
        }

        return out
    }

    private fun addFileToJar(file: File, node: String, jos: JarOutputStream) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { f ->
                addFileToJar(f, node + file.name + "/", jos)
            }
        } else {
            val entry = JarEntry(node + file.name)
            copyEntry(entry, file, jos)
        }
    }

    private fun addFileToZip(file: File, node: String, zos: ZipOutputStream) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { f ->
                addFileToZip(f, node + file.name + "/", zos)
            }
        } else {
            val entry = ZipEntry(node + file.name)
            copyEntry(entry, file, zos)
        }
    }

    private fun copyEntry(entry: ZipEntry, file: File, zos: ZipOutputStream) {
        zos.putNextEntry(entry)

        file.inputStream().use { fos ->
            val bytes = ByteArray(10240)
            var len: Int
            while (true) {
                len = fos.read(bytes)
                if (len == -1) break
                zos.write(bytes, 0, len)
            }

            zos.closeEntry()
        }
    }

    private fun fileSize(size: Long): String {
        val gb = 1024 * 1024 * 1024 //定义GB的计算常量

        val mb = 1024 * 1024 //定义MB的计算常量

        val kb = 1024 //定义KB的计算常量

        // 格式化小数
        // 格式化小数
        val df = DecimalFormat("0.00")

        return if (size / gb >= 1) {
            //如果当前Byte的值大于等于1GB
            df.format(size / gb.toFloat()) + "GB"
        } else if (size / mb >= 1) {
            //如果当前Byte的值大于等于1MB
            df.format(size / mb.toFloat()) + "MB"
        } else if (size / kb >= 1) {
            //如果当前Byte的值大于等于1KB
            df.format(size / kb.toFloat()) + "KB"
        } else {
            size.toString() + "B"
        }
    }
}