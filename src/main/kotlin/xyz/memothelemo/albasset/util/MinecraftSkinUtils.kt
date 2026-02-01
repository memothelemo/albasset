package xyz.memothelemo.albasset.util

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val HEAD_ICON_BASE_URL: String = "https://minotar.net/avatar/"

fun ServerPlayer.getHeadIconUrl(): String =
    HEAD_ICON_BASE_URL + URLEncoder.encode(this.plainTextName, StandardCharsets.UTF_8)

fun CommandSourceStack.getHeadIconUrl(): String? = this.player?.getHeadIconUrl()
