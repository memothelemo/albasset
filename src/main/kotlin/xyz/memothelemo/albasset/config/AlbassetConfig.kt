package xyz.memothelemo.albasset.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.source.decodeFromStream
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import xyz.memothelemo.albasset.Albasset
import xyz.memothelemo.albasset.Albasset.logger
import xyz.memothelemo.albasset.util.AssertionException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Objects

@Serializable
class AlbassetConfig {
    val alerts: AlertsConfig = AlertsConfig()
    var discord: DiscordConfig? = null
        private set

    private fun fixFieldInconsistencies() {
        // Set config.discord = null if the user wishes to disable it.
        this.discord.let { discord ->
            if (discord == null) return@let
            if (!discord.isEnabled()) {
                logger.debug("Fixed configuration inconsistency: 'discord.enabled' field")
                this.discord = null
            }
        }
    }

    companion object {
        private const val FILE_NAME = "${Albasset.MOD_ID}.toml"

        val modConfigFile: File = FabricLoader.getInstance()
            .configDir
            .resolve(FILE_NAME)
            .toFile()

        private val deserializer = Toml(
            inputConfig = TomlInputConfig(ignoreUnknownNames = true),
            outputConfig = TomlOutputConfig(indentation = TomlIndentation.FOUR_SPACES)
        )

        /** Saves the default config file (found in resource) to a specific path. */
        fun copyDefault(path: Path) {
            Albasset::class.java.getResourceAsStream("/$FILE_NAME").use { input ->
                if (input == null) {
                    logger.error("$FILE_NAME resource not found!")
                    throw AssertionException()
                }
                Files.copy(input, path, StandardCopyOption.REPLACE_EXISTING)
                logger.debug("Saved default config file at {}", path)
            }
        }

        /**
         * Attempts to load an Albasset configuration from a file. It will save the
         * file if the specified path does not actually exist.
         *
         * @return Albasset configuration instance. It may return null if it failed to load
         *         from the configuration file. The error details is logged to the logger.
         */
        fun load(): AlbassetConfig? {
            try {
                // Try to create directories with the file's parent directory
                val parentDir = modConfigFile.parentFile
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    logger.warn("Failed to create missing folders at {}", parentDir)
                }

                val existsBefore = modConfigFile.exists()
                if (!existsBefore) {
                    logger.info("Cannot find config file, creating default one...");
                    copyDefault(modConfigFile.toPath())
                }

                // Finally, we load the config file with TOML.
                val config = deserializer.decodeFromStream<AlbassetConfig>(modConfigFile.inputStream())
                config.fixFieldInconsistencies()
                return config
            } catch (ex: Exception) {
                logger.warn("Failed to load config file in $modConfigFile", ex)
                return null
            }
        }
    }
}
