package com.dhsdevelopments.potato.channelmessages.settings;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.StorageHelper;

public class ChannelSettingsActivity extends AppCompatActivity
{
    public static final String EXTRA_CHANNEL_ID = "org.atari.dhs.potato.channel_id";

    private CheckBox showNotificationsCheckbox;
    private CheckBox notifyUnreadCheckbox;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_channel_settings );

        showNotificationsCheckbox = (CheckBox)findViewById( R.id.display_notifications_checkbox );
        notifyUnreadCheckbox = (CheckBox)findViewById( R.id.notify_unread_checkbox );

        boolean showNotifications = false;
        boolean notifyUnread = false;

        String channelId = getIntent().getStringExtra( EXTRA_CHANNEL_ID );
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        try(Cursor result = db.query( StorageHelper.CHANNEL_CONFIG_TABLE,
                                  new String[] { StorageHelper.CHANNEL_CONFIG_SHOW_NOTIFICATIONS,
                                                 StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD },
                                  StorageHelper.CHANNEL_CONFIG_ID + " = ?", new String[] { channelId },
                                  null, null, null, null ) ) {
            if( result.moveToNext() ) {
                showNotifications = result.getInt( 0 ) != 0;
                notifyUnread = result.getInt( 1 ) != 0;
            }
        }

        showNotificationsCheckbox.setChecked( showNotifications );
        notifyUnreadCheckbox.setChecked( notifyUnread );
    }
}
