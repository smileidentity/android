package com.smileidentity.networking.serializer

import com.smileidentity.networking.models.JobType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object JobTypeSerializer : KSerializer<JobType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("JobType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: JobType) = encoder.encodeInt(value.value)

    override fun deserialize(decoder: Decoder): JobType =
        JobType.Companion.fromValue(decoder.decodeInt())
}
