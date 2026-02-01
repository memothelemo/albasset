package xyz.memothelemo.albasset.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

fun Logger.setMinimumLogLevel(minLevel: Level) {
    check(this is org.apache.logging.log4j.core.Logger) {
        "`logger` is not an instance of org.apache.logging.log4j.core.Logger"
    }

    val appenders = this.appenders
    this.isAdditive = false
    Configurator.setLevel(this, minLevel)
    appenders.values.forEach { this.addAppender(it) }
}
