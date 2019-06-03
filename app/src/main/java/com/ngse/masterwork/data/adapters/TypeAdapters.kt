package com.ngse.masterwork.data.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.ngse.masterwork.Constants
import java.io.IOException
import java.util.*


class CalendarTypeAdapter : TypeAdapter<Calendar>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Calendar?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(Constants.DATE_TIME_FORMAT.format(value.time))
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Calendar? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        var calendar: Calendar? = null
        val text = `in`.nextString()
        try {
            val ms = Constants.DATE_TIME_FORMAT.parse(text).time
            calendar = Calendar.getInstance()
            calendar!!.timeInMillis = ms
        } catch (e: Exception) {
        }

        return calendar
    }
}

