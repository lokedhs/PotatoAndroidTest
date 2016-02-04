package com.dhsdevelopments.potato.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.StorageHelper;

import java.util.ArrayList;
import java.util.List;

public class DomainUtils
{
    private final SQLiteDatabase db;

    public DomainUtils( Context context ) {
        db = PotatoApplication.getInstance( context ).getCacheDatabase();
    }

    public List<DomainDescriptor> loadDomains() {
        List<DomainDescriptor> domains = new ArrayList<>();
        try( Cursor result = db.query( StorageHelper.DOMAINS_TABLE,
                                       new String[] { StorageHelper.DOMAINS_ID, StorageHelper.DOMAINS_NAME },
                                       null, null, null, null, null, null ) ) {
            while( result.moveToNext() ) {
                String id = result.getString( 0 );
                String name = result.getString( 1 );
                domains.add( new DomainDescriptor( id, name ) );
            }
        }
        return domains;
    }
}
