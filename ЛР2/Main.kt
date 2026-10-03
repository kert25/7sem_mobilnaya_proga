fun invertCase(text: String): String {
    return text.map { char ->
        when {
            char.isLowerCase() -> char.uppercaseChar()
            char.isUpperCase() -> char.lowercaseChar()
            else -> char
        }
    }.joinToString("")
}

fun main() {
    val strings = listOf(
        "Hello, Kotlin!",
        "ПрИвЕт, Мир!",
        "AbC123 xYz",
        "Смешанный ТЕКСТ.",
        "already lower / УЖЕ ВЕРХНИЙ"
    )

    strings.forEachIndexed { index, text ->
        println("Строка ${index + 1}: $text")
        println("Результат: ${invertCase(text)}")
    }
}
