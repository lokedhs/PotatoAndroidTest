package com.dhsdevelopments.potato.imagecache;

import android.support.annotation.Nullable;

import java.io.File;

public class CachedFileResult
{
    @Nullable
    private File file;

    public CachedFileResult( @Nullable File file ) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "CachedFileResult[" +
                       "file=" + file +
                       ']';
    }
}
