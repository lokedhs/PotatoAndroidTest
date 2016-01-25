package com.dhsdevelopments.potato.channelmessages;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChannelContentAdapter extends RecyclerView.Adapter<ChannelContentAdapter.ViewHolder>
{
    private static final int VIEW_TYPE_PLAIN_MESSAGE = 0;
    private static final int VIEW_TYPE_EXTRA_CONTENT = 1;
    private static final int NUM_VIEW_TYPES = 2;

    private Context context;
    private String cid;

    private MessageFormat dateFormat;
    private SimpleDateFormat isoDateFormat;

    private List<MessageWrapper> messages = new ArrayList<>();

    public ChannelContentAdapter( Context context, String cid ) {
        this.context = context;
        this.cid = cid;

        dateFormat = new MessageFormat( context.getResources().getString( R.string.message_entry_date_label ) );
        isoDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        isoDateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    public void setMessages( List<Message> messages ) {
        List<MessageWrapper> result = new ArrayList<>( messages.size() );
        for( Message m : messages ) {
            result.add( new MessageWrapper( m, isoDateFormat, dateFormat ) );
        }
        this.messages = result;
        notifyDataSetChanged();
        Log.i( "Updated " + this.messages.size() + " messages" );
    }

    public void loadMessages() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<MessageHistoryResult> call = app.getPotatoApi().loadHistoryAsJson( app.getApiKey(), cid, 10 );
        call.enqueue( new Callback<MessageHistoryResult>()
        {
            @Override
            public void onResponse( Response<MessageHistoryResult> response, Retrofit retrofit ) {
                setMessages( response.body().getMessages() );
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Failed to load message history", t );
            }
        } );
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch( viewType ) {
            case VIEW_TYPE_PLAIN_MESSAGE:
                return new ViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_basic, parent, false ) );
            case VIEW_TYPE_EXTRA_CONTENT:
                return new ViewHolderExtraContent( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_extra_html, parent, false ) );
            default:
                throw new IllegalStateException( "Unexpected viewType: " + viewType );
        }
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        holder.fillInView( messages.get( position ) );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType( int position ) {
        MessageWrapper m = messages.get( position );
        if( m.getExtraHtml() == null ) {
            return VIEW_TYPE_PLAIN_MESSAGE;
        }
        else {
            return VIEW_TYPE_EXTRA_CONTENT;
        }
    }

    /**
     * Called when a new message is received from the server.
     */
    public void newMessage( Message msg ) {
        MessageWrapper w = new MessageWrapper( msg, isoDateFormat, dateFormat );
        int pos = Collections.binarySearch( messages, w, new Comparator<MessageWrapper>()
        {
            @Override
            public int compare( MessageWrapper m1, MessageWrapper m2 ) {
                return m1.getCreatedDate().compareTo( m2.getCreatedDate() );
            }
        } );

        if( msg.updated == null ) {
            // This is a new message, add it at the appropriate position.
            // Don't do this if the message is already in the list.
            if( pos < 0 ) {
                int insertionPos = -pos - 1;
                messages.add( insertionPos, w );
                notifyItemInserted( insertionPos );
            }
        }
        else {
            // This is an update. Only update if the message is already in the log.
            if( pos >= 0 ) {
                if( msg.deleted ) {
                    messages.remove( pos );
                    notifyItemRemoved( pos );
                }
                else {
                    messages.set( pos, w );
                    notifyItemChanged( pos );
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView senderView;
        private TextView dateView;
        private TextView contentView;

        public ViewHolder( View itemView ) {
            super( itemView );

            senderView = (TextView)itemView.findViewById( R.id.sender );
            dateView = (TextView)itemView.findViewById( R.id.date );
            contentView = (TextView)itemView.findViewById( R.id.content );
        }

        public void fillInView( MessageWrapper message ) {
            senderView.setText( message.getSenderName() );
            dateView.setText( message.getCreatedDateFormatted() );
            contentView.setText( message.getMarkupContent() );
        }
    }

    public class ViewHolderExtraContent extends ViewHolder
    {
        private TextView htmlContentView;

        public ViewHolderExtraContent( View itemView ) {
            super( itemView );
            htmlContentView = (TextView)itemView.findViewById( R.id.extra_content_html );
        }

        @Override
        public void fillInView( MessageWrapper message ) {
            super.fillInView( message );
            htmlContentView.setText( Html.fromHtml( message.getExtraHtml() ) );
        }
    }
}
