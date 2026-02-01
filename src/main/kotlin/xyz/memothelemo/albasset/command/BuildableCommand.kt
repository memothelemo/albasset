package xyz.memothelemo.albasset.command

import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

interface BuildableCommand {
    fun build(): LiteralCommandNode<CommandSourceStack>
}
