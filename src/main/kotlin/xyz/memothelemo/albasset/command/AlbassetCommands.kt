package xyz.memothelemo.albasset.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import xyz.memothelemo.albasset.Albasset
import xyz.memothelemo.albasset.command.subcommands.ReloadCommand

object AlbassetCommands {
    const val PERMISSION_PREFIX = "${Albasset.MOD_ID}.commands"

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val root = Commands.literal(Albasset.MOD_ID).build()
        dispatcher.root.addChild(root)

        root.addChild(ReloadCommand.build())
    }
}
