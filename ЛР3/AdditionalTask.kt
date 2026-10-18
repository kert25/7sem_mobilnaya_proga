data class ElectronicDevice(
    val model: String,
    val manufacturer: String,
    val batteryLevel: Int
) {
    var isPoweredOn: Boolean = false
        private set

    init {
        require(batteryLevel in 0..100) {
            "Уровень заряда должен быть в диапазоне от 0 до 100"
        }
    }

    fun powerOn() {
        isPoweredOn = true
        println("$model включено")
    }

    fun powerOff() {
        isPoweredOn = false
        println("$model выключено")
    }

    fun description(): String {
        return "Устройство: $manufacturer $model, заряд: $batteryLevel%"
    }

    fun operate() {
        println("$model выполняет базовую работу")
    }
}

fun main() {
    val device = ElectronicDevice(
        model = "Galaxy S24",
        manufacturer = "Samsung",
        batteryLevel = 95
    )

    println(device.description())
    device.powerOn()
    device.operate()
    println("Состояние питания: ${if (device.isPoweredOn) "включено" else "выключено"}")

    val sameDevice = ElectronicDevice("Galaxy S24", "Samsung", 95)
    println("Устройства равны: ${device == sameDevice}")

    val chargedDevice = device.copy(batteryLevel = 100)
    println("Копия с полным зарядом: $chargedDevice")

    val (model, manufacturer, batteryLevel) = device
    println("Модель: $model, производитель: $manufacturer, заряд: $batteryLevel%")

    device.powerOff()

    println(device)
}
