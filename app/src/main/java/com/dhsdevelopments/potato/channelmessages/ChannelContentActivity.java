package com.dhsdevelopments.potato.channelmessages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;
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

    private ChannelUsersTracker usersTracker;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        Intent intent = getIntent();
        String channelId = intent.getStringExtra( ChannelContentFragment.ARG_CHANNEL_ID );
        String channelName = intent.getStringExtra( ChannelContentFragment.ARG_CHANNEL_NAME );

        setContentView( R.layout.activity_channel_content );
        Toolbar toolbar = (Toolbar)findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

//        FloatingActionButton fab = (FloatingActionButton)findViewById( R.id.fab );
//        fab.setOnClickListener( new View.OnClickListener()
//        {
//            @Override
//            public void onClick( View view ) {
//                Snackbar.make( view, "Replace with your own detail action", Snackbar.LENGTH_LONG )
//                        .setAction( "Action", null ).show();
//            }
//        } );

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }

//        DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_content_drawer_layout );
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
//        drawer.setDrawerListener( toggle );
//        toggle.syncState();

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
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.channel_content_toolbar_menu, menu );
        return true;
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

    public ChannelUsersTracker getUsersTracker() {
        return usersTracker;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if( id == android.R.id.home ) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo( new Intent( this, ChannelListActivity.class ) );
            return true;
        }
        return super.onOptionsItemSelected( item );
    }
}
