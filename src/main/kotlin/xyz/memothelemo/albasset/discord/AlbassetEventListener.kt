package xyz.memothelemo.albasset.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.session.SessionRecreateEvent
import net.dv8tion.jda.api.events.session.SessionResumeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import xyz.memothelemo.albasset.config.Snowflake
import xyz.memothelemo.albasset.Albasset.logger;

class AlbassetEventListener(val bot: AlbassetBot): ListenerAdapter() {
    override fun onStatusChange(event: StatusChangeEvent) {
        logger.debug("[Discord] Status: {} -> {}", event.oldStatus, event.newStatus)

        // Alert if it is disconnected
        if (event.oldStatus == JDA.Status.CONNECTED && event.newStatus == JDA.Status.DISCONNECTED) {
            logger.warn("[Discord] Disconnected from gateway")
        }
    }

    override fun onSessionResume(event: SessionResumeEvent) {
        logger.info("[Discord] Session resumed")
    }

    override fun onSessionRecreate(event: SessionRecreateEvent) {
        // Self user is initialized since it is done internally by JDA.
        val botName = event.jda.selfUser.name
        logger.info("[Discord] Re-identified as {}", botName)
    }

    override fun onReady(event: ReadyEvent) {
        // Unlike `onSessionRecreate`, this instance is not fully initialized yet
        // since the main thread has to wait for AlbassetBot to instantiate and plug
        // the server session into the current listener.
        //
        // Self user is initialized since it is done internally by JDA.
        val botName = event.jda.selfUser.name
        logger.info("[Discord] Logged in as {}", botName)

        // Check if the bot is in the configured guild ID set in config.bot.server_id
        val mainGuildId: Snowflake = this.bot.config.guildId
        if (!mainGuildId.isValid()) {
            logger.warn("Albasset requires to specify the main Discord server/guild ID in the config " +
                "(bot.server_id). Please set it up to remove this warning message.")
        } else if (this.bot.isPresentInGuild(mainGuildId)) {
            logger.debug("The bot is present in the guild ({})", mainGuildId)
        } else {
            logger.debug("The bot is not present in the guild ({})", mainGuildId)
            logger.warn("Albasset requires the Discord bot to be joined in the server specified in the config "
                    + "(bot.server_id). Please add the bot to the guild to remove this warning message.")
        }
    }
}