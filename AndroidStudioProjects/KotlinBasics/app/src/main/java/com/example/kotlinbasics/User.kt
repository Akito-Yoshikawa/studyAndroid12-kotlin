package com.example.kotlinbasics

data class User(val id: Long, val name: String)

fun main() {
    val user1 = User(1, "yoshikawa")
    val user2 = User(2, "kimiyoshi")

    // Data同士を比べる。下記二つは同じ
    println(user1 == user2)
    println(user1.equals(user2))

    // copy methodがあり、Dataクラスをコピーすることが出来る。一部を書き換えてコピーすることも可能
    val updatedUser1 = user1.copy(name = "hurukawa")
    println(user1)
    println(updatedUser1)

    println(updatedUser1.component1()) // print 1
    println(updatedUser1.component2()) // print hurukawa

    // 分解宣言
    val (id, name) = updatedUser1
    println("id= $id, name= $name")
}