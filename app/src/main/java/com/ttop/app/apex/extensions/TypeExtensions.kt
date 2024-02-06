package com.ttop.app.apex.extensions

fun String.appendChar(afterIndex: Int, afterChar: Char): String {
    val sb = StringBuilder(this);
    sb.insert(afterIndex + 1, afterChar);
    return sb.toString()
}