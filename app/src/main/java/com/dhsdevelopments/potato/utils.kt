package com.dhsdevelopments.potato

import java.io.File

fun <T> nlazy(getter: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getter()
    }
}

fun makeRandomCharacterSequence(buf: StringBuilder, n: Int) {
    for (i in 0..19) {
        buf.append(('a' + (Math.random() * ('z' - 'a' + 1)).toInt()).toChar())
    }
}

fun makeRandomFile(dir: File, tmpFilePrefix: String = ""): File {
    val buf = StringBuilder()
    buf.append(tmpFilePrefix)
    makeRandomCharacterSequence(buf, 20)
    buf.append('_')
    val s = buf.toString()
    for (i in 0..29) {
        val name = s + i
        val f = File(dir, name)
        if (f.createNewFile()) {
            return f
        }
    }

    throw IllegalStateException("Unable to create temp file")
}
