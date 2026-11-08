pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MobileLabs"

// Один Gradle-проект на все лабораторные. Android Studio открывает корень
// папки, а каждая лабораторная — отдельный модуль со своей конфигурацией
// запуска и отладки.
//
// ЛР1–ЛР3 — консольный Kotlin. Исходники остаются в папках ЛР1..ЛР3, модули
// в labs/ только подключают их к сборке. В каждой лабораторной два файла со
// своим fun main(), поэтому один файл — один модуль: иначе компилятор видит
// конфликт перегрузок main().

listOf("1", "2", "3").forEach { number ->
    val labDir = "labs/lr$number"
    include(":lr$number")
    project(":lr$number").projectDir = file(labDir)
    listOf("main", "additional").forEach { part ->
        include(":lr$number:$part")
        project(":lr$number:$part").projectDir = file("$labDir/$part")
    }
}

// ЛР4–ЛР8 — Android-приложения. Модуль :lrN:app указывает прямо на папку app
// внутри папки лабораторной, поэтому исходники, ресурсы и отчёты остаются
// на своих местах.
mapOf("4" to "ЛР4", "5" to "ЛР5", "6" to "ЛР6", "7" to "ЛР7", "8" to "ЛР8").forEach { (number, labDir) ->
    include(":lr$number")
    project(":lr$number").projectDir = file(labDir)
    include(":lr$number:app")
    project(":lr$number:app").projectDir = file("$labDir/app")
}
