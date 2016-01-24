package com.dhsdevelopments.potato.channelmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channellist.ChannelListActivity;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest;
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult;
import com.dhsdevelopments.potato.editor.UidSpan;
import com.dhsdevelopments.potato.editor.UserNameSuggestAdapter;
import com.dhsdevelopments.potato.editor.UserNameTokeniser;
import com.dhsdevelopments.potato.service.ChannelSubscriptionService;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.IOException;

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
    private UserNameSuggestAdapter userNameSuggestAdapter;

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED  );
        intentFilter.addAction( ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE );
        getContext().registerReceiver( receiver, intentFilter );

        adapter = new ChannelContentAdapter( getContext(), cid );
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver( receiver );
        super.onDestroy();
    }

    private void handleBroadcastMessage( Intent intent ) {
        Log.i( "received broadcast message of type " + intent.getAction() );
        switch( intent.getAction() ) {
            case ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED:
                Message msg = (Message)intent.getSerializableExtra( ChannelSubscriptionService.EXTRA_MESSAGE );
                if( msg.channel.equals( cid ) ) {
                    adapter.newMessage( msg );
                }
                break;
            case ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE:
                ChannelUsersTracker tracker = ChannelUsersTracker.findEnclosingUserTracker( this );
                if( tracker != null ) {
                    tracker.processIncoming( intent );
                }
                break;
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
        View rootView = inflater.inflate( R.layout.fragment_channel_content, container, false );
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
        adapter.registerAdapterDataObserver( observer );

        final MultiAutoCompleteTextView messageInput = (MultiAutoCompleteTextView)rootView.findViewById( R.id.message_input_field );
        messageInput.setImeActionLabel( "Send", KeyEvent.KEYCODE_ENTER );
        messageInput.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event ) {
                if( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    sendMessage( messageInput );
                    return true;
                }
                else {
                    return false;
                }
            }
        } );

        ChannelUsersTracker userTracker = ChannelUsersTracker.findEnclosingUserTracker( this );
        userNameSuggestAdapter = new UserNameSuggestAdapter( getContext(), userTracker );
        messageInput.setAdapter( userNameSuggestAdapter );
        messageInput.setTokenizer( new UserNameTokeniser( userTracker ) );

        final InputMethodManager imm = (InputMethodManager)getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
        Button sendButton = (Button)rootView.findViewById( R.id.send_button );
        sendButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v ) {
                sendMessage( messageInput );
                imm.hideSoftInputFromWindow( messageInput.getWindowToken(), 0 );
            }
        } );

        return rootView;
    }

    @Override
    public void onDestroyView() {
        userNameSuggestAdapter.shutdown();
        adapter.unregisterAdapterDataObserver( observer );
        super.onDestroyView();
    }

    private void sendMessage( final EditText messageInput ) {
        CharSequence text = messageInput.getText();

        if( text.length() > 0 ) {
            PotatoApplication app = PotatoApplication.getInstance( getContext() );
            PotatoApi api = app.getPotatoApi();
            String apiKey = app.getApiKey();
            Call<SendMessageResult> call = api.sendMessage( apiKey, cid, new SendMessageRequest( convertUidRefs( text ) ) );
            call.enqueue( new Callback<SendMessageResult>()
            {
                @Override
                public void onResponse( Response<SendMessageResult> response, Retrofit retrofit ) {
                    if( response.isSuccess() ) {
                        Log.i( "Created message with id: " + response.body().id );
                    }
                    else {
                        try {
                            Log.e( "Send message error from server: " + response.errorBody().string() );
                        }
                        catch( IOException e ) {
                            Log.e( "Exception when getting error body after sending message", e );
                        }
                        displaySnackbarMessage( messageInput, "The server responded with an error" );
                    }
                }

                @Override
                public void onFailure( Throwable t ) {
                    Log.e( "Error sending message to channel", t );
                    displaySnackbarMessage( messageInput, "Error sending message: " + t.getMessage() );
                }
            } );

            messageInput.setText( "" );
        }
    }

    private String convertUidRefs( CharSequence text ) {
        if( text instanceof Spanned ) {
            Spanned spannedText = (Spanned)text;

            StringBuilder buf = new StringBuilder();
            int length = spannedText.length();
            UidSpan[] spans = spannedText.getSpans( 0, length, UidSpan.class );
            int pos = 0;
            for( UidSpan span : spans ) {
                int start = spannedText.getSpanStart( span );
                if( start > pos ) {
                    buf.append( spannedText.subSequence( pos, start ) );
                }
                buf.append( "\uDB80\uDC01user:" );
                buf.append( span.getUid() );
                buf.append( ":" );
                buf.append( span.getName() );
                buf.append( "\uDB80\uDC01" );
                pos = spannedText.getSpanEnd( span );
            }
            if( pos < length ) {
                buf.append( spannedText.subSequence( pos, length ) );
            }
            return buf.toString();
        }
        else {
            return text.toString();
        }
    }

    private void displaySnackbarMessage( View view, String message ) {
        Snackbar.make( view, message, Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
    }
}
