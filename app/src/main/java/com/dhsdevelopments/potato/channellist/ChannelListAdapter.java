package com.dhsdevelopments.potato.channellist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity;
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment;
import com.dhsdevelopments.potato.clientapi.channel.Domain;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

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
                    arguments.putString( ChannelContentFragment.ARG_CHANNEL_ID, holder.item.getId() );
                    arguments.putString( ChannelContentFragment.ARG_CHANNEL_NAME, holder.item.getName() );
                    ChannelContentFragment fragment = new ChannelContentFragment();
                    fragment.setArguments( arguments );

                    parent.getSupportFragmentManager()
                          .beginTransaction()
                          .replace( R.id.channel_detail_container, fragment )
                          .commit();
                }
                else {
                    Context context = v.getContext();
                    Intent intent = new Intent( context, ChannelContentActivity.class );
                    intent.putExtra( ChannelContentFragment.ARG_CHANNEL_ID, holder.item.getId() );
                    intent.putExtra( ChannelContentFragment.ARG_CHANNEL_NAME, holder.item.getName() );
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
        PotatoApplication app = PotatoApplication.getInstance( parent );
        Call<List<Domain>> call = app.getPotatoApi().getChannels( app.getApiKey() );
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
