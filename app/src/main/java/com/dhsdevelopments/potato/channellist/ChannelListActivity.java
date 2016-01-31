package com.dhsdevelopments.potato.channellist;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.StorageHelper;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;
import com.dhsdevelopments.potato.channelmessages.HasUserTracker;
import com.dhsdevelopments.potato.clientapi.channel2.ChannelsResult;
import com.dhsdevelopments.potato.clientapi.channel2.Domain;
import com.dhsdevelopments.potato.service.RemoteRequestService;
import com.dhsdevelopments.potato.settings.SettingsActivity;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

import java.util.List;

/**
 * An activity representing a list of Channels. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ChannelContentActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ChannelListActivity extends AppCompatActivity implements HasUserTracker
{
    public static final String EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id";
    public static final String EXTRA_DOMAIN_NAME = "com.dhsdevelopments.potato.domain_name";

    private String selectedDomainId = null;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;

    /**
     * In two pane mode, this activity needs to hold the channel users tracker for the selected channel.
     */
    private ChannelUsersTracker usersTracker;
    private ChannelListAdapter channelListAdapter;
    private SubMenu domainsMenu;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_channel_list );

        selectedDomainId = getIntent().getStringExtra( EXTRA_DOMAIN_ID );

        Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        NavigationView navigationView = (NavigationView)findViewById( R.id.channel_list_nav_view );
        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected( MenuItem item ) {
                return handleNavigationItemSelected( item );
            }
        } );
        domainsMenu = navigationView.getMenu().findItem( R.id.nav_domain_menu ).getSubMenu();

        View recyclerView = findViewById( R.id.channel_list );
        assert recyclerView != null;
        setupRecyclerView( (RecyclerView)recyclerView );

        if( findViewById( R.id.channel_detail_container ) != null ) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }

        DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_list_drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        updateDomainList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_list_drawer_layout );
        if( drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        }
        else {
            super.onBackPressed();
        }
    }

    private boolean handleNavigationItemSelected( MenuItem item ) {
        DrawerLayout drawer = (DrawerLayout)findViewById( R.id.channel_list_drawer_layout );
        drawer.closeDrawer( GravityCompat.START );

        switch( item.getItemId() ) {
            case R.id.nav_settings:
                startActivity( new Intent( this, SettingsActivity.class ) );
                break;
            default:
                checkMenuAndSwitchToDomain( item );
        }

        return true;
    }

    private void checkMenuAndSwitchToDomain( MenuItem item ) {
        Intent intent = item.getIntent();
        if( intent != null ) {
            String domainId = intent.getStringExtra( EXTRA_DOMAIN_ID );
            if( domainId != null ) {
                selectDomain( domainId );
            }
        }
    }

    private void selectDomain( String domainId ) {
        String domainName = null;
        selectedDomainId = domainId;
        int n = domainsMenu.size();
        for( int i = 0 ; i < n ; i++ ) {
            MenuItem item = domainsMenu.getItem( i );
            boolean isSelectedDomain = item.getIntent().getStringExtra( EXTRA_DOMAIN_ID ).equals( domainId );
            if( isSelectedDomain ) {
                domainName = item.getIntent().getStringExtra( EXTRA_DOMAIN_NAME );
            }
            item.setChecked( isSelectedDomain );
        }
        if( domainName != null ) {
            setTitle( domainName );
        }
        channelListAdapter.selectDomain( domainId );
    }

    private void setupRecyclerView( @NonNull RecyclerView recyclerView ) {
        channelListAdapter = new ChannelListAdapter( this );
        recyclerView.setAdapter( channelListAdapter );
    }

    public boolean isTwoPane() {
        return twoPane;
    }

    public void setActiveChannel( String channelId ) {
        usersTracker = ChannelUsersTracker.findForChannel( this, channelId );
    }

    @Override
    public ChannelUsersTracker getUsersTracker() {
        return usersTracker;
    }

    public void updateDomainList() {
        SQLiteDatabase db = PotatoApplication.getInstance( this ).getCacheDatabase();

        domainsMenu.clear();

        try( Cursor result = db.query( StorageHelper.DOMAINS_TABLE,
                                       new String[] { StorageHelper.DOMAINS_ID, StorageHelper.DOMAINS_NAME },
                                       null, null, null, null, StorageHelper.DOMAINS_NAME ) ) {
            while( result.moveToNext() ) {
                String domainId = result.getString( 0 );
                String domainName = result.getString( 1 );

                MenuItem item = domainsMenu.add( domainName );
                Intent intent = new Intent();
                intent.putExtra( EXTRA_DOMAIN_ID, domainId );
                intent.putExtra( EXTRA_DOMAIN_NAME, domainName );
                item.setIntent( intent );
            }
        }
    }
}
