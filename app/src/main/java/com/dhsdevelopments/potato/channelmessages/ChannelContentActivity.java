package com.dhsdevelopments.potato.channelmessages;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.StorageHelper;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;
import com.dhsdevelopments.potato.service.RemoteRequestService;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;
import com.dhsdevelopments.potato.userlist.UserListFragment;

/**
 * An activity representing a single Channel detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ChannelListActivity}.
 */
public class ChannelContentActivity extends AppCompatActivity implements HasUserTracker
{
    private static final int SELECT_IMAGE_RESULT_CODE = 1;

    private ChannelUsersTracker usersTracker;
    private String channelId;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        Intent intent = getIntent();
        channelId = intent.getStringExtra( ChannelContentFragment.ARG_CHANNEL_ID );
        String channelName = intent.getStringExtra( ChannelContentFragment.ARG_CHANNEL_NAME );

        setContentView( R.layout.activity_channel_content );
        Toolbar toolbar = (Toolbar)findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }

        usersTracker = ChannelUsersTracker.findForChannel( this, channelId );

        UserListFragment userListFragment = UserListFragment.newInstance( channelId );
        getSupportFragmentManager()
                .beginTransaction()
                .replace( R.id.user_list_container, userListFragment )
                .commit();

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if( savedInstanceState == null ) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

            arguments.putString( ChannelContentFragment.ARG_CHANNEL_ID, channelId );
            arguments.putString( ChannelContentFragment.ARG_CHANNEL_NAME, channelName );

            setTitle( channelName );

            ChannelContentFragment channelContentFragment = new ChannelContentFragment();
            channelContentFragment.setArguments( arguments );
            getSupportFragmentManager().beginTransaction()
                                       .add( R.id.channel_detail_container, channelContentFragment )
                                       .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_content_drawer_layout );
        if( drawer.isDrawerOpen( GravityCompat.END ) ) {
            drawer.closeDrawer( GravityCompat.END );
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.channel_content_toolbar_menu, menu );

        MenuItem notifyUnreadOption = menu.findItem( R.id.menu_option_notify_unread );
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        boolean notifyUnread = false;
        try( Cursor cursor = queryForChannel( db ) ) {
            if( cursor.moveToNext() ) {
                notifyUnread = cursor.getInt( 0 ) != 0;
            }
        }
        notifyUnreadOption.setChecked( notifyUnread );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch( item.getItemId() ) {
            case android.R.id.home:
                navigateUpTo( new Intent( this, ChannelListActivity.class ) );
                return true;
            case R.id.menu_option_show_users:
                DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_content_drawer_layout );
                if( drawer.isDrawerOpen( GravityCompat.END ) ) {
                    drawer.closeDrawer( GravityCompat.END );
                }
                else {
                    drawer.openDrawer( GravityCompat.END );
                }
                return true;
            case R.id.menu_option_notify_unread:
                boolean notifyUnread = !item.isChecked();
                item.setChecked( notifyUnread );
                updateNotifyUnreadSetting( notifyUnread );
                return true;
            case R.id.menu_option_send_image:
                sendImage();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void sendImage() {
        Intent intent = new Intent();
        intent.setAction( Intent.ACTION_GET_CONTENT );
        intent.setType( "image/*" );
        startActivityForResult( Intent.createChooser( intent, getString( R.string.chooser_title_select_image ) ), SELECT_IMAGE_RESULT_CODE );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( resultCode == RESULT_OK ) {
            if( requestCode == SELECT_IMAGE_RESULT_CODE ) {
                Uri uri = data.getData();
                RemoteRequestService.Companion.sendMessageWithImage( this, channelId, uri );
            }
        }
    }

    public String getPath( Uri uri ) {
        String[] projection = { MediaStore.Images.Media.DATA };
        //CursorLoader loader = new CursorLoader( this, uri, projection, null, null, null );

        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        Cursor cursor = managedQuery( uri, projection, null, null, null );
        if( cursor != null ) {
            int column_index = cursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
            cursor.moveToFirst();
            return cursor.getString( column_index );
        }
        // this is our fallback here
        return uri.getPath();
    }

    public ChannelUsersTracker getUsersTracker() {
        return usersTracker;
    }

    private Cursor queryForChannel( SQLiteDatabase db ) {
        return db.query( StorageHelper.CHANNEL_CONFIG_TABLE,
                         new String[] { StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD },
                         StorageHelper.CHANNEL_CONFIG_ID + " = ?", new String[] { channelId },
                         null, null, null, null );
    }

    private void updateNotifyUnreadSetting( boolean notifyUnread ) {
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();
        db.beginTransaction();
        try {
            boolean hasElement;
            try( Cursor result = queryForChannel( db ) ) {
                hasElement = result.moveToNext();
            }

            if( hasElement ) {
                ContentValues values = new ContentValues();
                values.put( StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD, notifyUnread ? 1 : 0 );
                db.update( StorageHelper.CHANNEL_CONFIG_TABLE,
                           values,
                           StorageHelper.CHANNEL_CONFIG_ID + " = ?", new String[] { channelId } );
            }
            else {
                ContentValues values = new ContentValues();
                values.put( StorageHelper.CHANNEL_CONFIG_ID, channelId );
                values.put( StorageHelper.CHANNEL_CONFIG_SHOW_NOTIFICATIONS, 0 );
                values.put( StorageHelper.CHANNEL_CONFIG_NOTIFY_UNREAD, notifyUnread ? 1 : 0 );
                db.insert( StorageHelper.CHANNEL_CONFIG_TABLE, null, values );
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        RemoteRequestService.Companion.updateUnreadSubscriptionState( this, channelId, notifyUnread );
    }
}
