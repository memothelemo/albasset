package xyz.memothelemo.albasset

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.MinecraftServer
import xyz.memothelemo.albasset.config.AlbassetConfig
import xyz.memothelemo.albasset.discord.AlbassetBot
import xyz.memothelemo.albasset.Albasset.logger
import xyz.memothelemo.albasset.alert.AdminCommandExecutedAlert
import xyz.memothelemo.albasset.discord.AlbassetEventListener
import xyz.memothelemo.albasset.discord.InitBotException
import xyz.memothelemo.albasset.util.sanitizeCommand
import java.time.Instant
import java.util.Objects

class AlbassetContext(val bot: AlbassetBot?, var config: AlbassetConfig, val server: MinecraftServer) {
    fun onAdminCommandExecuted(source: CommandSourceStack, command: String, timestamp: Instant) {
        if (!config.alerts.adminCommands.enabled) return

        // 1. Alert to the console
        logger.warn("Privileged command executed: command=`{}`, user=`{}`",
            command.sanitizeCommand(),
            source.textName
        );

        // 2. Send the message to the configured alert channel
        val bot = this.bot ?: return
        val alert = AdminCommandExecutedAlert(source, command, timestamp)
        bot.sendMessageToAlertChannel(alert)
    }

    fun softlyReplaceConfig(newConfig: AlbassetConfig) {
        this.config = newConfig
        if (this.config.discord != null && this.bot != null) {
            newConfig.discord.also {
                if (it == null) {
                    logger.error("newConfig.discord should not be null!")
                    throw AssertionError()
                }
                this.bot.config = it
            }
        }
    }

    /**
     * This function checks by comparing the newly loaded configuration
     * with the current one and determines whether Albasset mod needs to
     * reinitialize its own server context.
     */
    fun needsRestart(new: AlbassetConfig): Boolean {
        // New token means we need to restart and build another JDA instance again.
        if (config.discord != null && new.discord != null) {
            if (config.discord?.token != new.discord?.token) return true
        } else if (Objects.isNull(config.discord) != Objects.isNull(new.discord)) {
            return true
        }
        return false
    }

    fun close() {
        bot?.shutdown(false)
    }

    companion object {
        fun init(config: AlbassetConfig?, server: MinecraftServer): AlbassetContext? {
            var config: AlbassetConfig? = config
            if (config == null) {
                config = AlbassetConfig.load() ?: return null
            }
            logger.info("Loaded config at ${AlbassetConfig.modConfigFile}")

            var bot: AlbassetBot? = null
            config.discord.let { config ->
                // Don't proceed if it is disabled!
                if (config == null) {
                    logger.info("Discord integration is disabled")
                    return@let
                }

                logger.debug("Discord integration is enabled")
                try {
                    bot = AlbassetBot(config) { bot ->
                        bot.jda.addEventListener(AlbassetEventListener(bot))
                    }
                } catch (_: InitBotException) {
                    return null
                }
            }

            return AlbassetContext(bot, config, server)
        }
    }
}
