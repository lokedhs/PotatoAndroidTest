package com.dhsdevelopments.potato.common

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import java.io.File

fun makeRandomCharacterSequence(buf: Appendable, n: Int) {
    repeat(n) {
        buf.append('a' + (Math.random() * ('z' - 'a' + 1)).toInt())
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

abstract class BackgroundTask<T>(context: Context, val message: CharSequence) : AsyncTask<Unit, Unit, T>() {
    val dialog = ProgressDialog(context)

    override fun onPreExecute() {
        dialog.setMessage(message)
        dialog.show()
    }

    override fun onPostExecute(result: T) {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    abstract fun runTask(): T

    abstract fun taskFinished(result: T)

    override fun doInBackground(vararg params: Unit?): T = runTask()
}

/**
 * Converts a markup string into something that is more readable as a plain string.
 */
fun markupToPlain(s: String): String {
    val nameRegex = Regex("^user:([a-zA-Z0-9.@-]+):(.*)$")

    val buf = StringBuilder()
    var start = 0
    var i = 0

    val collectPart = {
        if (i > start) {
            buf.append(s.substring(start, i))
            start = i
        }
    }

    while (i < s.length) {
        if (s.codePointAt(i) == 0xf0001) {
            collectPart()
            i = s.offsetByCodePoints(i, 1)
            val nameStart = i
            while(i < s.length && s.codePointAt(i) != 0xf0001) {
                i = s.offsetByCodePoints(i, 1)
            }

            val result = nameRegex.matchEntire(s.substring(nameStart, i))
            if(result != null) {
                buf.append(result.groupValues[2])
            }
            else {
                buf.append("[illegal format]")
            }
            i = s.offsetByCodePoints(i, 1)
            start = i
        }
        else {
            i = s.offsetByCodePoints(i, 1)
        }
    }
    collectPart()

    return buf.toString()
}
