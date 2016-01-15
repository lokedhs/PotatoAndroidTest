package com.dhsdevelopments.potato.channelmessages;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageListAdapter implements ListAdapter
{
    private Context context;
    private String cid;
    private List<Message> messages = new ArrayList<>();
    private Set<DataSetObserver> observers = new HashSet<>();

    public MessageListAdapter( Context context, String cid ) {
        this.context = context;
        this.cid = cid;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled( int position ) {
        return true;
    }

    @Override
    public void registerDataSetObserver( DataSetObserver observer ) {
        observers.add( observer );
    }

    @Override
    public void unregisterDataSetObserver( DataSetObserver observer ) {
        observers.remove( observer );
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem( int position ) {
        return messages.get( position );
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        return null;
    }

    @Override
    public int getItemViewType( int position ) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public void loadMessages() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<MessageHistoryResult> call = app.getPotatoApi().loadHistoryAsJson( app.getApiKey(), cid, 10 );
        call.enqueue( new Callback<MessageHistoryResult>()
        {
            @Override
            public void onResponse( Response<MessageHistoryResult> response, Retrofit retrofit ) {
                Log.i( "Got messages: " + response.body() );
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Failed to load message history", t );
            }
        } );
    }
}
