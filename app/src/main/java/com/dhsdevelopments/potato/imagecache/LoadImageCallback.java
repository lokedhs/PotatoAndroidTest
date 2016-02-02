package com.dhsdevelopments.potato.imagecache;

import android.graphics.Bitmap;

public interface LoadImageCallback
{
    void bitmapLoaded( Bitmap bitmap );

    void bitmapNotFound();
}
