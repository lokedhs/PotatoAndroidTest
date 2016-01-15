package com.dhsdevelopments.potato;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;

/**
 * A fragment representing a single Channel detail screen.
 * This fragment is either contained in a {@link ChannelListActivity}
 * in two-pane mode (on tablets) or a {@link ChannelDetailActivity}
 * on handsets.
 */
public class ChannelDetailFragment extends Fragment
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
    public ChannelDetailFragment() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        if( getArguments().containsKey( ARG_CHANNEL_ID ) ) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
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
        View rootView = inflater.inflate( R.layout.channel_detail, container, false );

        // Show the dummy content as text in a TextView.
//        if( item != null ) {
//            ((TextView)rootView.findViewById( R.id.channel_detail )).setText( item.details );
//        }

        return rootView;
    }
}
