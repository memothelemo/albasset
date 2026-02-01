package xyz.memothelemo.albasset.util

import net.minecraft.util.StringUtil
import org.slf4j.helpers.MessageFormatter

/**
 * Formats the entire string depending on the arguments required
 * based on the template provided.
 */
fun String.format(arg1: Any?): String {
    return MessageFormatter.format(this, arg1).message
}

/**
 * Formats the entire string depending on the arguments required
 * based on the template provided.
 */
fun String.format(arg1: Any?, arg2: Any?): String {
    return MessageFormatter.format(this, arg1, arg2).message
}

/**
 * Formats the entire string depending on the arguments required
 * based on the template provided.
 */
fun String.format(vararg args: Any?): String {
    return MessageFormatter.arrayFormat(this, args).message
}

/**
 * Tries to sanitize commands by removing any control characters,
 * new lines, any Markdown output, and raw Minecraft section sign.
 * @return Possibly a sanitized command
 */
fun String.sanitizeCommand(): String {
    var safe = this.replace("[\\r\\n]+".toRegex(), "")
        .replace("[\\x00-\\x1F\\x7F]".toRegex(), "")
        .replace("§.".toRegex(), "")

    // Trimming any possible symbols that can be used for Markdown.
    // safe = safe.replace("([\\\\*_~`|>\\[\\]()])".toRegex(), "\\\\$1")
    safe = safe.replace("([\\\\`|>\\[\\]()])".toRegex(), "\\\\$1")

    // Remove excess whitespaces and any newlines
    return StringUtil.filterText(safe.trim { it <= ' ' })
}

