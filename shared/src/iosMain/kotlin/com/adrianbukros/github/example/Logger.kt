package com.adrianbukros.github.example

import timber.log.*
import platform.darwin.*
import kotlin.text.*
import platform.Foundation.*
import kotlin.native.*
import kotlin.native.concurrent.*

actual fun initTimber() {
    Timber.plant(MyNSLogTree(Timber.VERBOSE))
}

//class NSLogTree() : Tree() {
//    override fun isLoggable(priority: Int, tag: String?) = priority != Timber.VERBOSE
//
//    override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {
//        when (priority) {
//            Timber.ERROR -> formattedLog(Levels.ERROR, message)
//            Timber.ASSERT -> formattedLog(Levels.ASSERT, message)
//            Timber.WARNING -> formattedLog(Levels.WARNING, message)
//            Timber.INFO -> formattedLog(Levels.INFO, message)
//            Timber.DEBUG -> formattedLog(Levels.DEBUG, message)
//            Timber.VERBOSE -> formattedLog(Levels.VERBOSE, message)
//            else -> error("Unknown priority level: $priority")
//        }
//    }
//
//    private fun formattedLog(level: Levels, message: String?) {
//        NSLog("${level.emoji} \\e[1;${level.colorCode}${level.name}\\e[m  $message")
//    }
//
//    enum class Levels(val emoji: String, val colorCode: String) {
//        VERBOSE("💜", "251m"),
//        DEBUG("💚","35m"),
//        INFO("💙", "38m"),
//        WARNING("💛", "178m"),
//        ASSERT("❤️", "197m"),
//        ERROR("❤️", "197m")
//    }
//}

class MyNSLogTree(minPriority: Int) : NativeLogTree(minPriority) {
    override fun performLog(priority: Int, tag: String?, throwable: Throwable?, message: String?) {

        val bigMessage = StringBuilder()
        if (tag != null) {
            bigMessage.append("[$tag] ")
        }
        if (message != null) {
            bigMessage.append(message)
        }
        if (throwable != null) {
            val stacktrace: Array<String> = throwable.getStackTrace()

            bigMessage.append("\n${throwable.toString()}")
            for (element in stacktrace) {
                bigMessage.append("\n        at " + element)
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

    fun writeLog(level: LogLevels, message: String?) {
//        writeLog("${level.emoji} \\e[1;${level.colorCode}${level.name}\\e[m  $message")
        writeLog("${level.emoji}${level.name} $message")
    }

    override fun writeLog(s: String) {
        NSLog(s)
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