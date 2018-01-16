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
