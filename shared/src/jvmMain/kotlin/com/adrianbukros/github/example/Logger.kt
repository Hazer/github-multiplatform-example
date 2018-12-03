package com.adrianbukros.github.example

import timber.log.*

actual fun initTimber() {
    Timber.plant(SysouTree(Timber.VERBOSE))
}

class SysouTree(private val minPriority: Int) : Tree() {
    override fun isLoggable(priority: Int, tag: String?) = minPriority <= priority

    override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
        val bigMessage = StringBuilder()
        if (tag != null) {
            bigMessage.append("[$tag] ")
        }
        if (message != null) {
            bigMessage.append(message)
        }
        if (throwable != null) {
            val stacktrace = throwable.stackTrace.map { it.toString() }

            bigMessage.append("\n$throwable")
            for (element in stacktrace) {
                bigMessage.append("\n        at $element")
            }
        }

        val level = when (priority) {
            Timber.ERROR -> LogLevels.ERROR
            Timber.ASSERT -> LogLevels.ASSERT
            Timber.WARNING -> LogLevels.WARNING
            Timber.INFO -> LogLevels.INFO
            Timber.DEBUG -> LogLevels.DEBUG
            Timber.VERBOSE -> LogLevels.VERBOSE
            else -> LogLevels.ERROR
        }

        writeLog(level, bigMessage.toString())
    }

    private fun writeLog(level: Any, message: String) {
        System.out.println(message)
    }
}

enum class LogLevels(val emoji: String, val colorCode: String) {
    VERBOSE("💜", "251m"),
    DEBUG("💚", "35m"),
    INFO("💙", "38m"),
    WARNING("💛", "178m"),
    ASSERT("❤️", "197m"),
    ERROR("❤️", "197m")
}