package com.dhsdevelopments.potato.clientapi;

import android.content.Context;
import android.net.Uri;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import okio.BufferedSink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUriRequestBody extends RequestBody
{
    private Context context;
    private Uri imageUri;
    private MediaType mediaType;

    public ImageUriRequestBody( Context context, Uri imageUri ) {
        this.context = context;
        this.imageUri = imageUri;
        this.mediaType = MediaType.parse( context.getContentResolver().getType( imageUri ) );
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public void writeTo( BufferedSink sink ) throws IOException {
        try( InputStream in = context.getContentResolver().openInputStream( imageUri ) ) {
            OutputStream out = sink.outputStream();
            byte[] buf = new byte[16 * 1024];
            int n;
            while( (n = in.read( buf )) != -1 ) {
                out.write( buf, 0, n );
            }
        }
    }
}
