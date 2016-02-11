package com.dhsdevelopments.potato.imagecache

import java.io.File

class CachedFileResult(val file: File?) {
    override fun toString(): String {
        return "CachedFileResult[file=$file]"
    }
}
