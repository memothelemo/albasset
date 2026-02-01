package xyz.memothelemo.albasset

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import xyz.memothelemo.albasset.callback.AdminCommands
import xyz.memothelemo.albasset.command.AlbassetCommands
import xyz.memothelemo.albasset.config.AlbassetConfig
import xyz.memothelemo.albasset.util.AssertionException
import xyz.memothelemo.albasset.util.FabricUtils
import xyz.memothelemo.albasset.util.setMinimumLogLevel

object Albasset : ModInitializer {
    const val MOD_ID = "albasset"

    @JvmStatic
    val logger: Logger get() = LogManager.getLogger("Albasset");
    var context: AlbassetContext? = null
    val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName("Albasset"))

    override fun onInitialize() {
        if (debugMode()) {
            logger.setMinimumLogLevel(Level.DEBUG)
            logger.warn("Debug mode is enabled");
        } else if (FabricUtils.isRunningInClient()) {
            throw IllegalStateException("Albasset cannot run in client environment!");
        }

        AdminCommands.ExecuteCallback.EVENT.register { stack, string, instant -> context.let { context ->
            context?.onAdminCommandExecuted(stack, string, instant)
        } }

        ServerLifecycleEvents.SERVER_STARTING.register(::onServerStarting)
        ServerLifecycleEvents.SERVER_STOPPING.register(::onServerStopping)
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback {
            dispatcher, _, _ -> AlbassetCommands.register(dispatcher)
        })
    }

    fun relaunch(server: MinecraftServer): Deferred<Boolean> {
        logger.warn("User requested relaunch")

        // Reload the config and check if it needs total restart
        val newConfig = AlbassetConfig.load() ?: return scope.async { false }
        context.let { context ->
            if (context == null) return@let
            if (!context.needsRestart(newConfig)) {
                // Silently mutate current context's config with new one
                context.softlyReplaceConfig(newConfig)

                logger.info("Successfully applied changes to configuration; no full reload needed")
                return scope.async { true }
            }

            // Wait for the context to be flushed before we can start all over again
            logger.info("The new config has major changes; calling to restart Albasset")
            closeCurrentContext()
        }

        return launch(server, newConfig)
    }

    private fun closeCurrentContext() {
        logger.info("Shutting down Albasset...")

        val context = context ?: throw AssertionException()
        context.close()
        Albasset.context = null
    }

    private fun launch(server: MinecraftServer, config: AlbassetConfig?): Deferred<Boolean> {
        return scope.async(Dispatchers.IO) {
            logger.info("Launching Albasset...")

            val success = run {
                val newContext = AlbassetContext.init(config, server) ?: return@run false
                context = newContext
                logger.info("Albasset launched successfully");
                return@run true
            }

            if (!success) logger.warn("You may want to try to reload Albasset again " +
                "by running `/albasset restart` command");

            return@async success
        }
    }

    private fun debugMode(): Boolean =
        FabricLoader.getInstance().isDevelopmentEnvironment
            || System.getProperty("albasset.debug") != null

    private fun onServerStarting(server: MinecraftServer) {
        scope.launch {
            launch(server, null).await()
        }
    }

    @Suppress("unused")
    private fun onServerStopping(ignored: MinecraftServer) {
        closeCurrentContext()
    }
}
