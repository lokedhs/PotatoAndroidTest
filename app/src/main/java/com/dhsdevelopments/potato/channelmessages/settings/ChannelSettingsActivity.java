package com.dhsdevelopments.potato.channelmessages.settings;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.StorageHelper;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;

public class ChannelSettingsActivity extends AppCompatActivity
{
    public static final String EXTRA_CHANNEL_ID = "org.atari.dhs.potato.channel_id";

    private CheckBox showNotificationsCheckbox;
    private CheckBox notifyUnreadCheckbox;
    private String channelId;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_channel_settings );

        Toolbar toolbar = (Toolbar)findViewById( R.id.channel_settings_detail_toolbar );
        setSupportActionBar( toolbar );

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
//        if( actionBar != null ) {
//            actionBar.setDisplayHomeAsUpEnabled( true );
//        }

        showNotificationsCheckbox = (CheckBox)findViewById( R.id.display_notifications_checkbox );
        notifyUnreadCheckbox = (CheckBox)findViewById( R.id.notify_unread_checkbox );

        boolean showNotifications = false;
        boolean notifyUnread = false;

        channelId = getIntent().getStringExtra( EXTRA_CHANNEL_ID );
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        try( Cursor result = queryForChannel( db ) ) {
            if( result.moveToNext() ) {
                showNotifications = result.getInt( 0 ) != 0;
                notifyUnread = result.getInt( 1 ) != 0;
            }
        }

        showNotificationsCheckbox.setChecked( showNotifications );
        notifyUnreadCheckbox.setChecked( notifyUnread );
    }

    private Cursor queryForChannel( SQLiteDatabase db ) {
        return db.query( StorageHelper.CHANNEL_CONFIG_TABLE,
                         new String[] { StorageHelper.CHANNEL_CONFIG_SHOW_NOTIFICATIONS,
                                        StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD },
                         StorageHelper.CHANNEL_CONFIG_ID + " = ?", new String[] { channelId },
                         null, null, null, null );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.channel_settings_toolbar_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if( id == android.R.id.home ) {
            Log.w( "Should not get home button" );
            return true;
        }
        else if( id == R.id.menu_option_save ) {
            save();
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected( item );
        }
    }

    private void save() {
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        db.beginTransaction();
        try {
            boolean hasElement;
            try( Cursor result = queryForChannel( db ) ) {
                hasElement = result.moveToNext();
            }

            if( hasElement ) {
                ContentValues values = new ContentValues();
                fillInValues( values );
                db.update( StorageHelper.CHANNEL_CONFIG_TABLE,
                           values,
                           StorageHelper.CHANNEL_CONFIG_ID + " = ?", new String[] { channelId } );
            }
            else {
                ContentValues values = new ContentValues();
                values.put( StorageHelper.CHANNEL_CONFIG_ID, channelId );
                fillInValues( values );
                db.insert( StorageHelper.CHANNEL_CONFIG_TABLE, null, values );
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    private void fillInValues( ContentValues values ) {
        values.put( StorageHelper.CHANNEL_CONFIG_SHOW_NOTIFICATIONS, showNotificationsCheckbox.isChecked() ? 1 : 0 );
        values.put( StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD, notifyUnreadCheckbox.isChecked() ? 1 : 0 );
    }
}
