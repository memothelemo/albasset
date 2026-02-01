package xyz.memothelemo.albasset.util

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

object FabricUtils {
    fun isRunningInClient() = FabricLoader.getInstance().environmentType == EnvType.CLIENT
}
