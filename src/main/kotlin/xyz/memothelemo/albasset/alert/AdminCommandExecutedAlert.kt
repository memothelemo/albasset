package xyz.memothelemo.albasset.alert

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import xyz.memothelemo.albasset.util.format
import xyz.memothelemo.albasset.util.getHeadIconUrl
import xyz.memothelemo.albasset.util.sanitizeCommand
import java.awt.Color
import java.time.Instant

class AdminCommandExecutedAlert(
    val source: CommandSourceStack,
    val command: String,
    timestamp: Instant
): MessageCreateBuilder() {
    init {
        val embedBuilder = EmbedBuilder()
        var content = "{} used a privileged command!!"
        when (val player = source.player) {
            is ServerPlayer -> {
                content = content.format("**${player.plainTextName}**")
                this.appendPlayerInfoToEmbed(player, embedBuilder)
            }
            else -> {
                content = content.format("Someone")
                embedBuilder.setAuthor(source.textName)
            }
        }
        embedBuilder.setTitle("`/{}`".format(command.sanitizeCommand()))
        embedBuilder.setTimestamp(timestamp)

        this.setContent(content).addEmbeds(embedBuilder.build())
    }

    private fun appendPlayerInfoToEmbed(player: ServerPlayer, embed: EmbedBuilder) {
        val iconUrl = player.getHeadIconUrl()
        val currentPos = player.blockPosition()
        val description = EMBED_PLAYER_INFO_DESCRIPTION.format(
            player.uuid,
            player.level().dimension().identifier(),
            player.gameMode()?.serializedName ?: "<unknown>",
            currentPos.x, currentPos.y, currentPos.z,
        )

        embed.setAuthor(player.plainTextName, null, iconUrl)
        embed.setDescription(description)
    }

    companion object {
        private val EMBED_PLAYER_INFO_DESCRIPTION = listOf(
            "**UUID**: `{}`",
            "**Dimension**: `{}`",
            "**Game Mode**: `{}`",
            "**Position**: `{}, {}, {}`",
        ).joinToString("\n")
    }
}