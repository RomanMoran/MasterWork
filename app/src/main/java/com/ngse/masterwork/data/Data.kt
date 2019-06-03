package com.ngse.masterwork.data

import com.google.gson.annotations.SerializedName
import com.jjoe64.graphview.series.DataPointInterface
import com.ngse.masterwork.Constants
import java.io.Serializable
import java.text.ParseException


data class Parameters(
    val temperature: Int,
    val humidity: Int,
    val presetHumidity: Int,
    val startedPhoneTime: String?,
    val passedArduinoTime: Long
) {
    fun getLastSwitchedOn(): Long? {
        return if (!startedPhoneTime.isNullOrEmpty()) {
            try {
                var timemilis = Constants.DATE_TIME_FORMAT.parse(startedPhoneTime).time
                var lastSwitchOn = timemilis + passedArduinoTime
                lastSwitchOn
            } catch (e: ParseException) {
                null
            }
        } else null
    }
}

data class Temperature(
    @SerializedName("sensor_type")
    val sensorType: SensorType,
    @SerializedName("ds_temperature", alternate = ["dht_temperature"])
    val temperature: Float
)

data class DSTemperature(@SerializedName("ds_temperature") val temperature: Float)
data class DHTemperature(@SerializedName("dht_temperature") val temperature: Float)

data class ObjectToPass(val actionType: Int, val presetHumidity: Int? = null, val phoneTime: String? = null)

data class UltraSonicPass(val actionType: Int, val sonicType: Int, val pin: Int? = null, val state: Boolean? = null)

data class Humidity(val value: Int)

class CustomDataPoint(private var x: String, private var y: Double) : DataPointInterface, Serializable {
    override fun getX(): Double = x.toDouble()
    override fun getY(): Double = y
    override fun toString(): String = "[$x/$y]"
}

enum class STATUS {
    FAILED_CONNECTION,
    CONNECTED;

    companion object : List<STATUS> by STATUS.values().toList()
}

enum class LedState(val value: String) {
    ON("1"),
    OFF("0");

    companion object : List<LedState> by LedState.values().toList()
}