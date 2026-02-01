package xyz.memothelemo.albasset.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Represents Discord's ID type.
 * <p>
 * Snowflakes are unique identifiers for users, messages, guilds, etc., used within the Discord API.
 * They are represented as 64-bit unsigned integers.
 *
 * @param value The raw long value of a snowflake.
 */
@Serializable(with = SnowflakeSerializer::class)
data class Snowflake(val value: Long) {
    /**
     * Checks whether this ID is valid. This is useful to determine
     * whether the user has set anything to a field.
     */
    fun isValid() = value > 0
}

object SnowflakeSerializer: KSerializer<Snowflake> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Snowflake", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Snowflake) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Snowflake {
        val value = decoder.decodeString().toLong()
        if (value < 0) {
            error("Discord IDs must not be negative!");
        }
        return Snowflake(value)
    }
}
