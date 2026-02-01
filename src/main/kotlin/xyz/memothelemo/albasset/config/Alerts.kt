package xyz.memothelemo.albasset.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertsConfig(
    @SerialName("admin_commands")
    val adminCommands: AdminCommands = AdminCommands(),
)

@Serializable
data class AdminCommands(
    val enabled: Boolean = true,
)