package com.dhsdevelopments.potato.channellist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;
import com.dhsdevelopments.potato.channelmessages.HasUserTracker;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

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
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;

    /**
     * In two pane mode, this activity needs to hold the channel users tracker for the selected channel.
     */
    private ChannelUsersTracker usersTracker;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_channel_list );

        Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        FloatingActionButton fab = (FloatingActionButton)findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view ) {
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();
            }
        } );

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
    }

    private void setupRecyclerView( @NonNull RecyclerView recyclerView ) {
        recyclerView.setAdapter( new ChannelListAdapter( this ) );
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
}
