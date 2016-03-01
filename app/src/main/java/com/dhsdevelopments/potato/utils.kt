package com.dhsdevelopments.potato

import android.os.Handler
import com.dhsdevelopments.potato.clientapi.RemoteResult
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
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

fun <T : RemoteResult> callService(call: Call<T>, errorCallback: (String) -> Unit, successCallback: (T) -> Unit) {
    val result = call.execute()
    if (result.isSuccess) {
        val body = result.body()
        val errMsg = body.errorMsg()
        if (errMsg == null) {
            successCallback(body)
        }
        else {
            errorCallback(errMsg)
        }
    }
    else {
        errorCallback("Call failed, code: ${result.code()}, message: ${result.message()}")
    }
}

fun <T : RemoteResult> callServiceBackground(call: Call<T>, errorCallback: (String) -> Unit, successCallback: (T) -> Unit) {
    val handler = Handler()
    call.enqueue(object: Callback<T> {
        override fun onResponse(response: Response<T>, retrofit: Retrofit) {
            handler.post {
                if(response.isSuccess) {
                    val errorMessage = response.body().errorMsg()
                    if (errorMessage == null) {
                        successCallback(response.body())
                    }
                    else {
                        errorCallback(errorMessage)
                    }
                }
                else {
                    errorCallback("Call failed, code: ${response.code()}, message: ${response.message()}")
                }
            }
        }

        override fun onFailure(exception: Throwable) {
            Log.e("Exception when calling remote service", exception)
            handler.post {
                errorCallback("Connection error: ${exception.message}")
            }
        }
    } )
}
