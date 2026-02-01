package xyz.memothelemo.albasset.command.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlinx.coroutines.launch
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.PermissionLevel
import xyz.memothelemo.albasset.Albasset
import xyz.memothelemo.albasset.command.AlbassetCommands
import xyz.memothelemo.albasset.command.BuildableCommand
import xyz.memothelemo.albasset.util.prependServerHeader

object ReloadCommand: BuildableCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("reload")
            .requires(Permissions.require("${AlbassetCommands.PERMISSION_PREFIX}.restart", PermissionLevel.OWNERS))
            .executes { this.reload(it) }
            .build()
    }

    private fun reload(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        source.sendSystemMessage(Component.literal("Reloading Albasset...").prependServerHeader())

        Albasset.scope.launch {
            val success = Albasset.relaunch(context.source.server).await()
            val component = when (success) {
                true -> Component.literal("Albasset reloaded successfully")
                    .withStyle(ChatFormatting.GREEN)
                    .prependServerHeader()
                false -> Component.literal("Failed to reload Albasset! Please see the console " +
                    "logs for more details and try again if needed.")
                    .withStyle(ChatFormatting.RED)
                    .prependServerHeader()
            }
            source.sendSystemMessage(component)
            return@launch
        }

        return 0
    }
}
