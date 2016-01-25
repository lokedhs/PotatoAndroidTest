package com.dhsdevelopments.potato.imagecache;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;

class BitmapCacheEntry
{
    Bitmap bitmap;
    boolean loading;
    List<LoadImageCallback> callbacks;

    BitmapCacheEntry( boolean loading ) {
        this.loading = loading;
    }

    public void addCallback( LoadImageCallback callback ) {
        if( callbacks == null ) {
            callbacks = new ArrayList<>();
        }
        callbacks.add( callback );
    }
}
