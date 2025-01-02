package com.smileidentity.compose.nav

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
class PersistentMapSerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>,
) : KSerializer<ImmutableMap<K, V>> {
    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor = SerialDescriptor(
        "kotlinx.serialization.immutable.persistentMap",
        mapSerializer.descriptor,
    )

    override fun serialize(encoder: Encoder, value: ImmutableMap<K, V>) =
        mapSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): ImmutableMap<K, V> =
        mapSerializer.deserialize(decoder).toPersistentMap()
}
