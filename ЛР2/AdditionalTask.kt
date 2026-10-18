private const val RUSSIAN_ALPHABET = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"

fun invertCase(text: String): String {
    return text.map { char ->
        when {
            char.isLowerCase() -> char.uppercaseChar()
            char.isUpperCase() -> char.lowercaseChar()
            else -> char
        }
    }.joinToString("")
}

fun wordWeight(word: String): Int {
    return word.lowercase().sumOf { char ->
        RUSSIAN_ALPHABET.indexOf(char) + 1
    }
}

fun main() {
    val strings = listOf(
        "АБ",
        "Смешанный текст.",
        "Кот и собака",
        "Ёлка зимой",
        "Разработка программ"
    )

    strings.forEachIndexed { index, text ->
        println("Строка ${index + 1}: $text")
        println("Результат: ${invertCase(text)}")

        Regex("[А-Яа-яЁё]+").findAll(text).forEach { match ->
            val word = match.value
            println("Вес слова \"$word\": ${wordWeight(word)}")
        }
    }
}
