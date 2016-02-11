package com.dhsdevelopments.potato.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.StorageHelper
import java.util.*

class DomainUtils(context: Context) {
    private val db: SQLiteDatabase

    init {
        db = PotatoApplication.getInstance(context).cacheDatabase
    }

    fun loadDomains(): List<DomainDescriptor> {
        val domains = ArrayList<DomainDescriptor>()
        db.query(StorageHelper.DOMAINS_TABLE,
                arrayOf(StorageHelper.DOMAINS_ID, StorageHelper.DOMAINS_NAME),
                null, null, null, null, null, null).use { result ->
            while (result.moveToNext()) {
                val id = result.getString(0)
                val name = result.getString(1)
                domains.add(DomainDescriptor(id, name))
            }
        }
        return domains
    }
}
