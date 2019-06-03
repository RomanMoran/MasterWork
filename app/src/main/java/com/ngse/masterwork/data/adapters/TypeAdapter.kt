package com.ngse.masterwork.data.adapters

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.*

inline fun <reified T : Any> GsonBuilder.registerIntTypeAdapter(
    noinline getKey: (T) -> Int,
    noinline getVal: (Int) -> T?) = apply {
    registerTypeAdapter(T::class.java, IntTypeAdapter(getKey, getVal))
}
inline fun <reified T : Any> GsonBuilder.registerStringTypeAdapter(
    noinline getKey: (T) -> String,
    noinline getVal: (String) -> T?) = apply {
    registerTypeAdapter(T::class.java, StringTypeAdapter(getKey, getVal))
}

class IntTypeAdapter<T : Any>(
    private val getKey: (T) -> Int,
    private val getVal: (Int) -> T?
) : TypeAdapter<T>() {
    override fun read(reader: JsonReader) = reader.readInt(getVal)
    override fun write(writer: JsonWriter, value: T?) = writer.writeInt(value, getKey)
}

class StringTypeAdapter<T : Any>(
    private val getKey: (T) -> String,
    private val getVal: (String) -> T?
) : TypeAdapter<T>() {
    override fun read(reader: JsonReader) = reader.readString(getVal)
    override fun write(writer: JsonWriter, value: T?) = writer.writeString(value, getKey)
}

private fun <T : Any> JsonReader.readInt(getVal: (Int) -> T?): T? =
    if (peek() == JsonToken.NULL) run { nextNull(); null }
    else getVal(nextInt())

private fun <T : Any> JsonWriter.writeInt(value: T?, getKey: (T) -> Int) {
    value?.let { value(getKey(it)) } ?: nullValue()
}
private fun <T : Any> JsonReader.readString(getVal: (String) -> T?): T? =
    if (peek() == JsonToken.NULL) run { nextNull(); null }
    else getVal(nextString())

private fun <T : Any> JsonWriter.writeString(value: T?, getKey: (T) -> String) {
    value?.let { value(getKey(it)) } ?: nullValue()
}