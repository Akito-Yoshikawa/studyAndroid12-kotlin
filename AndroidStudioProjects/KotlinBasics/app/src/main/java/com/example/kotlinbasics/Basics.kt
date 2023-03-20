package com.example.kotlinbasics

data class Fruit(val name: String, val price: Double)

fun main() {

    // Array初期化
//    val numbers: IntArray = intArrayOf(1,2,3,4,5,6)
//    val numbers2 = intArrayOf(1,2,3,4,5,6)
//    // arrayofだと色々なデータ型を持つことができる。
//    // Intだけとか使用するデータ型が分かっていればintArrayとかで宣言した方が良い。
//    val numbers3 = arrayOf(1,2,3,4,5,6, "")
//
//    // contentToString()で、配列の中身をString型で出力してくれる!!
//    print(numbers3.contentToString())
//
//    // 配列アクセスは、他の言語と同じく配列[要素数]
//    print(numbers[0])
//
//    var fruits = arrayOf(Fruit("apple", 150.0), Fruit("orange", 200.0))
//    // array.indicesで配列の長さが分かる。
//    for (index in fruits.indices) {
////        print("${fruits[index].name} is in index $index ")
//    }
//
//    // swiftと同じ配列操作可能
//    for (fruit in fruits) {
////        print("${fruit.name}")
//    }

    // list型
//    val months = listOf("January", "February", "March")
//    var anyTypes = listOf(1,2,3,true,false,"String")
//    // listのサイズ取得
//    println(months.size)
//    print(months[0])
//
//    val addionalMonths = months.toMutableList()
//    val newMonths = arrayOf("April", "May", "June")
//    addionalMonths.addAll(newMonths)
//    addionalMonths.add("July")
//    print(addionalMonths)
//
//    val days = mutableListOf<String>("Mon","Tue","Wed")
//    days.add("Thu")
//    days[2] = "Sunday"
//    print(days)

    // setOf 重複された値は削除される
    val fruits = setOf("Orange","Apple","Grape","Apple")
    print(fruits.toSortedSet())

    val newFruits = fruits.toMutableList()
    newFruits.add("Water Melon")
    newFruits.add("Pear")
    println(newFruits.elementAt(4))

    // map swiftだと辞書
    val daysOfTheWeek = mapOf(1 to "Monday", 2 to "Tuesday", 3 to "Wednesday")

    for (key in daysOfTheWeek.keys) {
        print(" $key is to ${daysOfTheWeek[key]} ")
    }

    val fruitsMap = mapOf("Favorite" to Fruit("Grape", 2.5), "OK" to Fruit("Apple", 1.0))

    val newDaysOfWeek = daysOfTheWeek.toMutableMap()
    newDaysOfWeek[4] = "Thursday"
    newDaysOfWeek[5] = "Friday"

    print(newDaysOfWeek.toSortedMap())



//    var obj1: Any = "文字列です。"
//
//    // 文字列型にキャスト。失敗した場合は落ちる。
//    var str1: String = obj1 as String
//    println(str1.length)
//
//    var obj2: Any = 1337
//
//    // swiftと同じく、nullを許容する場合は"as?"をつけてキャスト
//    var str2: String? = obj2 as? String
//    println(str2) // prints null
//
//    var nullableName: String? = "yoshikawa"
////    nullableName = null
//
//    var name = nullableName ?: "Guest"
////    println("$name")
//
//    nullableName!!.lowercase()
}

