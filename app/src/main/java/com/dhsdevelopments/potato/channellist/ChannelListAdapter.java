package com.dhsdevelopments.potato.channellist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.*;
import com.dhsdevelopments.potato.clientapi.Domain;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.*;

import java.util.Collections;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder>
{
    private ChannelListActivity parent;
    private List<ChannelEntry> values = Collections.emptyList();

    public ChannelListAdapter( ChannelListActivity parent ) {
        this.parent = parent;
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
        loadItems();
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext() )
                                  .inflate( R.layout.channel_list_content, parent, false );
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder( final ViewHolder holder, int position ) {
        holder.item = values.get( position );
        holder.contentView.setText( values.get( position ).getName() );

        holder.view.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v ) {
                if( parent.isTwoPane() ) {
                    Bundle arguments = new Bundle();
                    arguments.putString( ChannelDetailFragment.ARG_CHANNEL_ID, holder.item.getId() );
                    arguments.putString( ChannelDetailFragment.ARG_CHANNEL_NAME, holder.item.getName() );
                    ChannelDetailFragment fragment = new ChannelDetailFragment();
                    fragment.setArguments( arguments );

                    parent.getSupportFragmentManager()
                          .beginTransaction()
                          .replace( R.id.channel_detail_container, fragment )
                          .commit();
                }
                else {
                    Context context = v.getContext();
                    Intent intent = new Intent( context, ChannelDetailActivity.class );
                    intent.putExtra( ChannelDetailFragment.ARG_CHANNEL_ID, holder.item.getId() );
                    intent.putExtra( ChannelDetailFragment.ARG_CHANNEL_NAME, holder.item.getName() );
                    context.startActivity( intent );
                }
            }
        } );
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    private void loadItems() {
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" ).create();
        Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl( "http://10.0.2.2:8080/api/1.0/" )
                                    .addConverterFactory( GsonConverterFactory.create( gson ) )
                                    .build();
        PotatoApi api = retrofit.create( PotatoApi.class );
        Call<List<Domain>> call = api.getChannels( PotatoApplication.getInstance( parent ).getApiKey() );
        call.enqueue( new Callback<List<Domain>>()
        {
            @Override
            public void onResponse( Response<List<Domain>> response, Retrofit retrofit ) {
                values = ChannelEntry.makeFromChannelTree( response.body() );
                notifyDataSetChanged();
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Got error from server", t );
            }
        } );

    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public View view;
        public TextView contentView;
        public ChannelEntry item;

        public ViewHolder( View view ) {
            super( view );
            this.view = view;
            contentView = (TextView)view.findViewById( R.id.content );
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
