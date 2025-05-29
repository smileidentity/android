package com.smileidentity.networking

import com.smileidentity.metadata.models.Value

object ValueParceler : kotlinx.parcelize.Parceler<Value> {
    override fun create(parcel: android.os.Parcel): Value {
        val type = parcel.readInt()
        return when (type) {
            0 -> Value.StringValue(parcel.readString()!!)
            1 -> Value.IntValue(parcel.readInt())
            2 -> Value.LongValue(parcel.readLong())
            3 -> Value.BoolValue(parcel.readByte() != 0.toByte())
            4 -> Value.DoubleValue(parcel.readDouble())
            5 -> {
                val size = parcel.readInt()
                val map = mutableMapOf<String, Value>()
                repeat(size) {
                    val key = parcel.readString()!!
                    val value = create(parcel)
                    map[key] = value
                }
                Value.ObjectValue(map)
            }
            else -> throw IllegalArgumentException("Unknown Value type")
        }
    }

    override fun Value.write(parcel: android.os.Parcel, flags: Int) {
        when (this) {
            is Value.StringValue -> {
                parcel.writeInt(0)
                parcel.writeString(value)
            }
            is Value.IntValue -> {
                parcel.writeInt(1)
                parcel.writeInt(value)
            }
            is Value.LongValue -> {
                parcel.writeInt(2)
                parcel.writeLong(value)
            }
            is Value.BoolValue -> {
                parcel.writeInt(3)
                parcel.writeByte(if (value) 1 else 0)
            }
            is Value.DoubleValue -> {
                parcel.writeInt(4)
                parcel.writeDouble(value)
            }
            is Value.ObjectValue -> {
                parcel.writeInt(5)
                parcel.writeInt(map.size)
                for ((k, v) in map) {
                    parcel.writeString(k)
                    v.write(parcel, flags)
                }
            }
        }
    }
}
