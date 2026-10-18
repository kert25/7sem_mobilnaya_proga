import kotlin.random.Random

fun main() {
    val numbers = IntArray(10) { Random.nextInt(-50, 52) * 2 + 1 }

    println("Массив случайных нечётных чисел: ${numbers.contentToString()}")

    val sumOfMultiplesOfFive = numbers.filter { it % 5 == 0 }.sum()
    println("Сумма элементов, кратных 5: $sumOfMultiplesOfFive")
}
