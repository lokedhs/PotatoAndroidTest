package com.dhsdevelopments.potato.channelmessages;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class ChannelContentAdapter extends RecyclerView.Adapter<ChannelContentAdapter.ViewHolder>
{
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
        View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.message, parent, false );
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        holder.fillInView( messages.get( position ) );
    }

    @Override
    public int getItemCount() {
        return messages.size();
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
}
