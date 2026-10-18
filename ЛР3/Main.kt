open class ElectronicDevice(
    val model: String,
    val manufacturer: String
) {
    var batteryLevel: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
        }

    var isPoweredOn: Boolean = false
        private set

    constructor(model: String) : this(model, "Неизвестный производитель")

    fun powerOn() {
        isPoweredOn = true
        println("$model включено")
    }

    fun powerOff() {
        isPoweredOn = false
        println("$model выключено")
    }

    open fun description(): String {
        return "Устройство: $manufacturer $model, заряд: $batteryLevel%"
    }

    open fun operate() {
        println("$model выполняет базовую работу")
    }
}

class Smartphone(
    model: String,
    manufacturer: String,
    var supports5G: Boolean
) : ElectronicDevice(model, manufacturer) {
    constructor(model: String, manufacturer: String) : this(model, manufacturer, false)

    override fun description(): String {
        val network = if (supports5G) "поддерживает 5G" else "не поддерживает 5G"
        return "Смартфон: $manufacturer $model, $network, заряд: $batteryLevel%"
    }

    override fun operate() {
        println("$model совершает звонок и запускает приложения")
    }
}

class Laptop(
    model: String,
    manufacturer: String,
    var screenSize: Double
) : ElectronicDevice(model, manufacturer) {
    constructor(model: String, manufacturer: String) : this(model, manufacturer, 15.6)

    override fun description(): String {
        return "Ноутбук: $manufacturer $model, экран: $screenSize дюйма, заряд: $batteryLevel%"
    }

    override fun operate() {
        println("$model запускает программу для разработки")
    }
}

fun main() {
    val smartphone = Smartphone("Galaxy S24", "Samsung", true)
    smartphone.batteryLevel = 95

    val laptop = Laptop("ThinkPad E14", "Lenovo")
    laptop.batteryLevel = 120

    val devices: List<ElectronicDevice> = listOf(smartphone, laptop)

    devices.forEach { device ->
        println(device.description())
        device.powerOn()
        device.operate()
        println("Состояние питания: ${if (device.isPoweredOn) "включено" else "выключено"}")
        device.powerOff()
        println()
    }

    println(smartphone)
}
