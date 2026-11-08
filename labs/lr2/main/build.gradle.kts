plugins {
    id("org.jetbrains.kotlin.jvm")
    application
}

// Исходник лабораторной остаётся в её папке: модуль только подключает файл
// к сборке, чтобы один fun main() не конфликтовал с другим.
kotlin.sourceSets["main"].kotlin.setSrcDirs(listOf(rootProject.file("ЛР2")))
kotlin.sourceSets["main"].kotlin.setIncludes(listOf("Main.kt"))

application {
    mainClass.set("MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Кириллица в выводе программы: без UTF-8 консоль показывает мусор.
tasks.named<JavaExec>("run") {
    jvmArgs("-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8")
}