package com.dhsdevelopments.potato.channelmessages;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;

/**
 * A fragment representing a single Channel detail screen.
 * This fragment is either contained in a {@link ChannelListActivity}
 * in two-pane mode (on tablets) or a {@link ChannelContentActivity}
 * on handsets.
 */
public class ChannelContentFragment extends Fragment
{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_CHANNEL_ID = "item_id";
    public static final String ARG_CHANNEL_NAME = "channel_name";

    private String cid;
    private String name;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChannelContentFragment() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        if( getArguments().containsKey( ARG_CHANNEL_ID ) ) {
            cid = getArguments().getString( ARG_CHANNEL_ID );
            name = getArguments().getString( ARG_CHANNEL_NAME );

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)activity.findViewById( R.id.toolbar_layout );
            if( appBarLayout != null ) {
                appBarLayout.setTitle( name );
            }
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.channel_content, container, false );
        RecyclerView messageListView = (RecyclerView)rootView.findViewById( R.id.message_list );

        LinearLayoutManager layoutManager = new LinearLayoutManager( this.getActivity() );
        messageListView.setLayoutManager( layoutManager );

        ChannelContentAdapter adapter = new ChannelContentAdapter( getContext(), cid );
        messageListView.setAdapter( adapter );
        adapter.loadMessages();

        TextView messageInput = (TextView)rootView.findViewById( R.id.message_input_field );

        return rootView;
    }
}
