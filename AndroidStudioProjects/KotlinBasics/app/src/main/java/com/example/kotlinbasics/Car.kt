package com.example.kotlinbasics

fun main() {
    val car = Car()
    println(car.myBrand)
    car.maxSpeed = 240
    println(car.maxSpeed)
}

class Car {
    lateinit var owner: String

    val myBrand: String = "BMW"
    get() {
        return  field.lowercase()
    }

    var maxSpeed = 250
    set(value) {
        field = if(value > 0) value else throw  IllegalArgumentException("0より大きい値を入力してください。")
    }

    init {
        this.owner = "yoshikawa"
    }
}