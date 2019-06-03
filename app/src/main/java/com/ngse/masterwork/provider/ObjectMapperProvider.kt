package com.ngse.masterwork.provider

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ngse.masterwork.data.SensorType
import com.ngse.masterwork.data.adapters.CalendarTypeAdapter
import com.ngse.masterwork.data.adapters.registerIntTypeAdapter
import java.util.*

object ObjectMapperProvider : IndependentProvider<Gson>() {

    override fun initInstance(): Gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Calendar::class.java, CalendarTypeAdapter())
        .registerTypeAdapter(GregorianCalendar::class.java, CalendarTypeAdapter())
        .registerIntTypeAdapter(SensorType::value, SensorType.IdMap::get)
        .create()
}


