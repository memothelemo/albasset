package xyz.memothelemo.albasset.callback

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.commands.CommandSourceStack
import java.time.Instant

interface AdminCommands {
    fun interface ExecuteCallback {
        fun executed(source: CommandSourceStack, command: String, timestamp: Instant)
        fun executed(source: CommandSourceStack, command: String) =
            executed(source, command, Instant.now())

        companion object {
            @JvmField
            val EVENT: Event<ExecuteCallback> = EventFactory.createArrayBacked(ExecuteCallback::class.java) { listeners ->
                ExecuteCallback { source, command, timestamp -> for (listener in listeners) listener.executed(source, command, timestamp)  }
            }
        }
    }
}