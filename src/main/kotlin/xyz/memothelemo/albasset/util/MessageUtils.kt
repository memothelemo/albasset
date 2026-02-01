package xyz.memothelemo.albasset.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

object MessageUtils {
    /** Default chat text style */
    private val normalStyle: Style
        get() = Style.EMPTY

    // TODO: Allow for custom server names aside from Dystopia
    private val header = Component.literal("[Dystopia] ")
        .withStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GOLD))

    /**
     * Prepends the server header from the component text literal.
     *
     * **Before**: `Hello, World!`
     *
     * **After**: `[Dystopia] Hello, World!`
     */
    fun prependServerHeader(component: Component): Component {
        // We need to create a separate component for this because it will apply
        // the rest of the component argument's style with header's style.
        return Component.empty().append(header).append(component) ?: throw AssertionException()
    }
}

fun Component.prependServerHeader(): Component {
    return MessageUtils.prependServerHeader(this)
}
