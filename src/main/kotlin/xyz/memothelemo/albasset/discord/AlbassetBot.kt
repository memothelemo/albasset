package xyz.memothelemo.albasset.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.exceptions.InvalidTokenException
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import xyz.memothelemo.albasset.Albasset.logger
import xyz.memothelemo.albasset.config.DiscordConfig
import xyz.memothelemo.albasset.config.Snowflake
import java.util.function.Consumer

class AlbassetBot(var config: DiscordConfig, modifier: Consumer<AlbassetBot>) {
    var jda: JDA

    fun sendMessageToAlertChannel(builder: MessageCreateBuilder) {
        val channelId = this.config.alertChannelId ?: return
        if (!channelId.isValid()) {
            logger.warn("[Discord] Failed to relay alert to Discord channel: " +
                "`discord.alert_channel_id` in config has an invalid Discord channel ID!")
            return
        }

        val channel = this.jda.getTextChannelById(channelId.value)
        if (channel == null) {
            logger.warn("[Discord] Failed to relay alert to Discord channel: cannot find channel {}", channelId)
            return
        }

        val message = builder.build()
        channel.sendMessage(message).queue({}) { throwable ->
            logger.warn("[Discord] Failed to relay alert to Discord channel!", throwable)
        }
    }

    /**
     * Checks whether JDA client has connected to the Discord gateway
     * successfully by returning a boolean value.
     */
    fun isConnected(): Boolean {
        return this.jda.status == JDA.Status.CONNECTED
    }

    /** Checks whether the bot is present in a specified guild. */
    fun isPresentInGuild(guildId: Snowflake): Boolean {
        val guildId = guildId.toString()
        return this.jda.guilds.any { it.id == guildId }
            || this.jda.unavailableGuilds.any { it == guildId }
    }

    /**
     * Attempts to shut down a Discord bot.
     * @param force Whether to shut down forcibly or gracefully.
     * @throws InitBotException
     */
    fun shutdown(force: Boolean) {
        logger.info("Killing JDA...")
        if (force) this.jda.shutdown() else this.jda.shutdownNow()

        try {
            logger.info("JDA was killed")
            this.jda.awaitShutdown()
        } catch (e: InterruptedException) {
            logger.warn("Failed to kill JDA")
        }
    }

    init {
        // Make sure the config.token is not either empty, blank or has whitespaces.
        if (!config.hasValidToken()) {
            logger.warn("Invalid token, please set correct token in the config file!");
            throw InitBotException()
        }

        val builder = JDABuilder.createDefault(config.token, INTENTS)
            .setAutoReconnect(true)
            .setEnableShutdownHook(true)
            .setEventPassthrough(true)

        while (true) {
            try {
                logger.debug("Connecting to Discord...");

                val jda = builder.build()
                this.jda = jda

                modifier.accept(this)
                jda.awaitReady()
                break
            } catch (e: Exception) {
                when (e) {
                    is InterruptedException, is IllegalStateException -> {
                        logger.warn("Failed to connect to Discord!", e);
                        throw InitBotException()
                    }
                    is InvalidTokenException -> {
                        if (e.message.equals("The provided token is invalid!")) {
                            logger.error("Invalid token, please set correct token in the config file!")
                            throw InitBotException()
                        }
                        logger.warn("Failed to connect to Discord, retrying...")
                        try {
                            Thread.sleep(6000)
                        } catch (_: InterruptedException) {
                            throw InitBotException()
                        }
                    }
                    else -> throw e
                }
            }
        }
    }
}

/**
 * Excepted exception to be thrown when trying
 * to initialize `AlbassetBot`
 */
class InitBotException : Exception()

private val INTENTS: Set<GatewayIntent> = setOf(
    GatewayIntent.DIRECT_MESSAGES,
    GatewayIntent.GUILD_MEMBERS
)
