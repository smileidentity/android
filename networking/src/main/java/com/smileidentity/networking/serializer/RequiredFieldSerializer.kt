package com.smileidentity.networking.serializer

import com.smileidentity.networking.models.RequiredField
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object RequiredFieldSerializer : KSerializer<RequiredField> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("RequiredField", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: RequiredField) =
        encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): RequiredField = try {
        RequiredField.valueOf(decoder.decodeString())
    } catch (_: Exception) {
        RequiredField.Unknown
    }
}
