package xyz.memothelemo.albasset.config;

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfig(
    private val enabled: Boolean = true,

    @Required
    val token: String,

    @Required
    @SerialName("server_id")
    val guildId: Snowflake,

    @SerialName("alert_channel_id")
    val alertChannelId: Snowflake? = null,
) {
    /**
     * Whether Discord integration should be enabled as
     * wished from the user.
     *
     * It is not advisable to use this method to determine if the user wants
     * to enable `Discord Integration`, please use `config.discord != null`
     * instead as AlbassetConfig automatically fixes any inconsistencies
     * especially to this field here.
     */
    fun isEnabled(): Boolean = enabled

    /**
     * This function checks whether the configured
     * Discord token is valid.
     */
    fun hasValidToken() = token.isNotBlank()
        && token.all { !it.isWhitespace() }
}
