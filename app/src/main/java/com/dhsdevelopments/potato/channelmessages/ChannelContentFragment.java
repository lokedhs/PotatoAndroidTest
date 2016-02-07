package com.dhsdevelopments.potato.channelmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;
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
import com.dhsdevelopments.potato.service.RemoteRequestService;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;
import org.jetbrains.annotations.NotNull;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.io.IOException;
import java.text.Collator;
import java.util.*;

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
    private Map<String, String> typingUsers = new HashMap<>();
    private Comparator<String> caseInsensitiveStringComparator;
    private TextView typingTextView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int lastVisibleItem;
    private RecyclerView messageListView;

    public ChannelContentFragment() {
        final Collator collator = Collator.getInstance();
        caseInsensitiveStringComparator = new Comparator<String>()
        {
            @Override
            public int compare( String o1, String o2 ) {
                return collator.compare( o1, o2 );
            }
        };
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
        intentFilter.addAction( ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED );
        intentFilter.addAction( ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE );
        intentFilter.addAction( ChannelSubscriptionService.ACTION_TYPING );
        LocalBroadcastManager.getInstance( getContext() ).registerReceiver( receiver, intentFilter );

        adapter = new ChannelContentAdapter( getContext(), cid );
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance( getContext() ).unregisterReceiver( receiver );
        super.onDestroy();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.fragment_channel_content, container, false );
        messageListView = (RecyclerView)rootView.findViewById( R.id.message_list );

        final LinearLayoutManager layoutManager = new LinearLayoutManager( this.getActivity() );
        messageListView.setLayoutManager( layoutManager );

        messageListView.setAdapter( adapter );

        observer = new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted( int positionStart, int itemCount ) {
                // Only scroll if the message was inserted at the bottom, and we're already looking at
                // the bottom element.
                Log.d( "rangeInserted. posStart=" + positionStart + ", count=" + itemCount + ", lastVis=" + lastVisibleItem + ", itemCount=" + adapter.getItemCount() );
                int numItems = adapter.getItemCount() - 1;
                if( lastVisibleItem >= numItems - itemCount - 1 && numItems == positionStart + itemCount ) {
                    Log.d( "scrolling view to " + (numItems + 1) );
                    messageListView.scrollToPosition( numItems );
                }
            }

            @Override
            public void onItemRangeChanged( int positionStart, int itemCount ) {
                // After a change, we want to ensure that we can still see the bottom element that
                // was visible before the change.
                messageListView.scrollToPosition( lastVisibleItem < adapter.getItemCount() - 1 ? lastVisibleItem + 1 : lastVisibleItem );
            }
        };
        adapter.registerAdapterDataObserver( observer );

        messageListView.addOnScrollListener( new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled( RecyclerView recyclerView, int dx, int dy ) {
                int pos = layoutManager.findLastVisibleItemPosition();
                lastVisibleItem = (pos == adapter.getItemCount() - 1) ? pos - 1 : pos;
            }
        } );

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

        typingTextView = (TextView)rootView.findViewById( R.id.typing_text_view );


        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById( R.id.channel_content_refresh );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh() {
                adapter.loadMoreMessages( new LoadMessagesCallback()
                {
                    @Override
                    public void loadSuccessful( @NotNull List<? extends MessageWrapper> messages ) {
                        swipeRefreshLayout.setRefreshing( false );
                        messageListView.scrollToPosition( adapter.positionForMessage( messages.get( messages.size() - 1 ).getId() ) );
                    }

                    @Override
                    public void loadFailed( @NotNull String errorMessage ) {
                        swipeRefreshLayout.setRefreshing( false );
                        showErrorSnackbar( "Error loading messages: " + errorMessage );
                    }
                } );
            }
        } );

        return rootView;
    }

    private void showErrorSnackbar( String message ) {
        Snackbar.make( messageListView, message, Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
    }

    @Override
    public void onDestroyView() {
        userNameSuggestAdapter.shutdown();
        adapter.unregisterAdapterDataObserver( observer );
        super.onDestroyView();
    }

    private void handleBroadcastMessage( Intent intent ) {
        Log.i( "received broadcast message of type " + intent.getAction() );
        switch( intent.getAction() ) {
            case ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED:
                processMessagePostedNotification( intent );
                break;
            case ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE:
                processChannelUsersNotification( intent );
                break;
            case ChannelSubscriptionService.ACTION_TYPING:
                processTypingNotification( intent );
                break;

        }
    }

    private void processMessagePostedNotification( Intent intent ) {
        Message msg = (Message)intent.getSerializableExtra( ChannelSubscriptionService.EXTRA_MESSAGE );
        if( msg.channel.equals( cid ) ) {
            adapter.newMessage( msg );
        }
    }

    private void processChannelUsersNotification( Intent intent ) {
        ChannelUsersTracker tracker = ChannelUsersTracker.findEnclosingUserTracker( this );
        if( tracker != null ) {
            tracker.processIncoming( intent );
        }
    }

    private void processTypingNotification( Intent intent ) {
        if( !intent.getStringExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID ).equals( cid ) ) {
            // Only process messages on this channel
            return;
        }

        String uid = intent.getStringExtra( ChannelSubscriptionService.EXTRA_USER_ID );
        String mode = intent.getStringExtra( ChannelSubscriptionService.EXTRA_TYPING_MODE );
        Log.i( "uid=" + uid + ", mode=" + mode );
        switch( mode ) {
            case ChannelSubscriptionService.TYPING_MODE_ADD:
                ChannelUsersTracker tracker = ChannelUsersTracker.findEnclosingUserTracker( this );
                typingUsers.put( uid, tracker.getNameForUid( uid ) );
                break;
            case ChannelSubscriptionService.TYPING_MODE_REMOVE:
                typingUsers.remove( uid );
                break;
            default:
                Log.w( "Unexpected typing mode in broadcast message: " + mode );
                break;
        }

        refreshTypingNotifier();
    }

    private void refreshTypingNotifier() {
        if( typingUsers.isEmpty() ) {
            typingTextView.setVisibility( View.INVISIBLE );
        }
        else {
            List<String> users = new ArrayList<>( typingUsers.values() );
            Collections.sort( users, caseInsensitiveStringComparator );

            StringBuilder buf = new StringBuilder();
            int numUsers = users.size();
            if( numUsers == 1 ) {
                buf.append( users.get( 0 ) );
                buf.append( " is typing" );
            }
            else {
                for( int i = 0 ; i < numUsers - 1 ; i++ ) {
                    buf.append( users.get( i ) );
                    if( i < numUsers - 2 ) {
                        buf.append( ", " );
                    }
                }
                buf.append( " and " );
                buf.append( users.get( numUsers - 1 ) );
                buf.append( " are typing" );
            }

            typingTextView.setText( buf.toString() );
            typingTextView.setVisibility( View.VISIBLE );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent( getContext(), ChannelSubscriptionService.class );
        intent.setAction( ChannelSubscriptionService.ACTION_BIND_TO_CHANNEL );
        intent.putExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid );
        getContext().startService( intent );
        adapter.loadMessages( new LoadMessagesCallback()
        {
            @Override
            public void loadSuccessful( @NotNull List<? extends MessageWrapper> messages ) {
                messageListView.scrollToPosition( adapter.getItemCount() - 1 );
            }

            @Override
            public void loadFailed( @NotNull String errorMessage ) {
                showErrorSnackbar( "Error loading messages: " + errorMessage );
            }
        } );

        refreshTypingNotifier();

        RemoteRequestService.Companion.markNotificationsForChannel( getContext(), cid );
    }

    @Override
    public void onStop() {
        Intent intent = new Intent( getContext(), ChannelSubscriptionService.class );
        intent.setAction( ChannelSubscriptionService.ACTION_UNBIND_FROM_CHANNEL );
        intent.putExtra( ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid );
        getContext().startService( intent );
        super.onStop();
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
