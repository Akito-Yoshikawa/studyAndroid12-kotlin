package com.example.kotlinbasics

fun main() {
    var myCar = OpenClassCar(200.0,"A3", "Audi")
    var myECar = ElectricCar(240.0,"S-Model", "Tesla", 85.0)

    myCar.drive(200.0)
    myECar.drive(200.0)
}

// Openをつけて継承可能にする
open class OpenClassCar(override val maxSpeed: Double, val name: String, val brand: String): Drivable {
    open var range = 0.0 // 子クラスでoverrideする場合はopenを使用する

    fun extendRange(amount: Double) {
        if (amount > 0) {
            range += amount
        }
    }

    override fun drive(): String {
        return "drive the interface drive"
    }

    open fun drive(distance: Double) {
        println("Drive for $distance KM")
    }
}

// : 親クラス()で継承する
class ElectricCar(maxSpeed: Double, name: String, brand: String, batteryLife: Double): OpenClassCar(maxSpeed, name, brand) {
    override var range = batteryLife * 6

    override fun drive(distance: Double) {
        println("Drive for $distance KM on electricity")
    }

    override fun drive(): String {
        return "drove for $range KM on electricity"
    }

    override fun brake() {
        super.brake()
    }
}
