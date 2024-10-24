import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.*

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.publish)
}
val jvmVersion = JavaVersion.VERSION_1_8.toString()

tasks.withType<JavaCompile> {
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = jvmVersion
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xjvm-default=all",
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

dependencies {
    implementation(libs.asm)
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Implementation-Version"] = project.version
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Built-Date"] = SimpleDateFormat("yyyy-MM-dd").format(Date())
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Built-Gradle"] = gradle.gradleVersion
    }
}

group = "cn.foxette.plugin.gradle"
version = "0.0.2"

gradlePlugin {
    plugins {
        create("PluginMaven") {
            id = "cn.foxette.plugin.gradle.junk-code"
            displayName = "Android Junk Code Generator"
            description = "A plugin generator junk code. It's reduce code similarity."
            implementationClass = "cn.foxette.plugin.gradle.jz.JunkCodePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/Lochinor"
    vcsUrl = "https://github.com/Lochinor/junk-code"
    tags = listOf("JunkCode", "Android")
}

val properties = Properties()

// 私服账号密码存放在 local.properties 中
val locale = File("./local.properties")
if (locale.exists()) {
    properties.load(locale.inputStream())
}

val nxName: String? = properties.getProperty("nexus.username")
val nxPwd: String? = properties.getProperty("nexus.password")

if (!nxName.isNullOrEmpty() && !nxPwd.isNullOrEmpty()) {
    publishing {
        publications {
            repositories {
                maven {
                    name = "junk-code"
                    setUrl("http://maven.foxaura.cn/repository/maven-nexus/")
                    credentials {
                        isAllowInsecureProtocol = true
                        username = nxName
                        password = nxPwd
                    }
                }
            }
        }
    }
}