package com.example.kotlinbasics

interface Drivable {
    val maxSpeed: Double
    fun drive(): String
    fun brake() {
        println("the drivable is braking")
    }
}

