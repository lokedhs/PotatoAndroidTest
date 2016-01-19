package com.dhsdevelopments.potato.channelmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.service.ChannelSubscriptionService;

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

    private BroadcastReceiver receiver;
    private ChannelContentAdapter adapter;
    private RecyclerView.AdapterDataObserver observer;

    public ChannelContentFragment() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        if( !getArguments().containsKey( ARG_CHANNEL_ID ) ) {
            throw new IllegalArgumentException( "channelId not specified in activity" );
        }

        cid = getArguments().getString( ARG_CHANNEL_ID );
        name = getArguments().getString( ARG_CHANNEL_NAME );

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive( Context context, Intent intent ) {
                handleBroadcastMessage( intent );
            }
        };
        IntentFilter intentFilter = new IntentFilter( ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED );
        getContext().registerReceiver( receiver, intentFilter);

        adapter = new ChannelContentAdapter( getContext(), cid );
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver( receiver );
        super.onDestroy();
    }

    private void handleBroadcastMessage( Intent intent ) {
        Message msg = (Message)intent.getSerializableExtra( ChannelSubscriptionService.EXTRA_MESSAGE );
        Log.i( "got broadcast message (" + cid + "): " + msg );
        if( msg.channel.equals( cid ) ) {
            adapter.newMessage( msg );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent( getContext(), ChannelSubscriptionService.class );
        intent.setAction( ChannelSubscriptionService.ACTION_BIND_TO_CHANNEL );
        intent.putExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid );
        getContext().startService( intent );
        adapter.loadMessages();
    }

    @Override
    public void onStop() {
        Intent intent = new Intent( getContext(), ChannelSubscriptionService.class );
        intent.setAction( ChannelSubscriptionService.ACTION_UNBIND_FROM_CHANNEL );
        intent.putExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid );
        getContext().startService( intent );
        super.onStop();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.channel_content, container, false );
        final RecyclerView messageListView = (RecyclerView)rootView.findViewById( R.id.message_list );

        LinearLayoutManager layoutManager = new LinearLayoutManager( this.getActivity() );
        messageListView.setLayoutManager( layoutManager );

        messageListView.setAdapter( adapter );

        observer = new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted( int positionStart, int itemCount ) {
                if( adapter.getItemCount() == positionStart + itemCount ) {
                    messageListView.scrollToPosition( positionStart + itemCount - 1 );
                }
            }
        };
        adapter.registerAdapterDataObserver(  observer );

        TextView messageInput = (TextView)rootView.findViewById( R.id.message_input_field );

        return rootView;
    }

    @Override
    public void onDestroyView() {
        adapter.unregisterAdapterDataObserver( observer );
        super.onDestroyView();
    }
}
