package com.dhsdevelopments.potato.clientapi

import android.content.Context
import android.net.Uri
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.RequestBody
import okio.BufferedSink
import java.io.IOException

class ImageUriRequestBody(private val context: Context, private val imageUri: Uri) : RequestBody() {
    private val mediaType: MediaType

    init {
        this.mediaType = MediaType.parse(context.contentResolver.getType(imageUri))
    }

    override fun contentType(): MediaType {
        return mediaType
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(imageUri).use { inStream ->
            val out = sink.outputStream()
            val buf = ByteArray(16 * 1024)
            while(true) {
                val n = inStream.read(buf)
                if(n == -1) {
                    break
                }
                out.write(buf, 0, n)
            }
        }
    }
}
