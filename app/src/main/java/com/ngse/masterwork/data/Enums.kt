package com.ngse.masterwork.data

enum class SensorType(var value: Int) {
    DS(0), DHT(1);

    companion object : List<SensorType> by values().toList()
    object IdMap : Map<Int, SensorType> by (values().associate { it.value to it })
}