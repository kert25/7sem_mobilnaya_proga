fun main() {
    val numbers = intArrayOf(-10, 0, 5, 12, 15, 22, 25, -3, 40, 7)

    println("Исходный массив: ${numbers.contentToString()}")

    val sumOfMultiplesOfFive = numbers.filter { it % 5 == 0 }.sum()
    println("Сумма элементов, кратных 5: $sumOfMultiplesOfFive")
}
