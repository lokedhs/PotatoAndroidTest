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
import com.dhsdevelopments.potato.clientapi.channel.Channel;
import com.dhsdevelopments.potato.clientapi.channel.Domain;
import com.dhsdevelopments.potato.clientapi.channel.Group;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ViewHolder>
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CHANNEL = 1;

    private ChannelListActivity parent;
    private List<ChannelEntry> publicChannels = Collections.emptyList();
    private List<ChannelEntry> privateChannels = Collections.emptyList();
    private List<Domain> channelTree = null;

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
        LayoutInflater layoutInflater = LayoutInflater.from( parent.getContext() );
        switch( viewType ) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder( layoutInflater.inflate( R.layout.channel_list_header, parent, false ) );
            case VIEW_TYPE_CHANNEL:
                return new ChannelViewHolder( layoutInflater.inflate( R.layout.channel_list_content, parent, false ) );
            default:
                throw new RuntimeException( "Unexpected view type=" + viewType );
        }
    }

    @Override
    public int getItemViewType( int position ) {
        if( publicChannels.isEmpty() ) {
            return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_CHANNEL;
        }
        else {
            if( position == 0 || (!privateChannels.isEmpty() && position == publicChannels.size() + 1) ) {
                return VIEW_TYPE_HEADER;
            }
            else {
                return VIEW_TYPE_CHANNEL;
            }
        }
    }

    @Override
    public void onBindViewHolder( final ViewHolder holder, int position ) {
        Log.d( "Binding view holder for position " + position + ", type=" + holder.getClass().getName() );
        if( publicChannels.isEmpty() ) {
            if( position == 0 ) {
                ((HeaderViewHolder)holder).setTitle( "Conversations" );
            }
            else {
                ((ChannelViewHolder)holder).fillInChannelEntry( privateChannels.get( position - 1 ) );
            }
        }
        else {
            if( position == 0 ) {
                ((HeaderViewHolder)holder).setTitle( "Channels" );
            }
            else if( position < publicChannels.size() + 1 ) {
                ((ChannelViewHolder)holder).fillInChannelEntry( publicChannels.get( position - 1 ) );
            }
            else if( !privateChannels.isEmpty() ) {
                if( position == publicChannels.size() + 1 ) {
                    ((HeaderViewHolder)holder).setTitle( "Conversations" );
                }
                else {
                    ((ChannelViewHolder)holder).fillInChannelEntry( privateChannels.get( position - publicChannels.size() - 2 ) );
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        int total = 0;
        if( !publicChannels.isEmpty() ) {
            total += publicChannels.size() + 1;
        }
        if( !privateChannels.isEmpty() ) {
            total += privateChannels.size() + 1;
        }
        return total;
    }

    private void loadItems() {
        PotatoApplication app = PotatoApplication.getInstance( parent );
        Call<List<Domain>> call = app.getPotatoApi().getChannels( app.getApiKey() );
        call.enqueue( new Callback<List<Domain>>()
        {
            @Override
            public void onResponse( Response<List<Domain>> response, Retrofit retrofit ) {
                channelTree = response.body();
                parent.channelTreeLoaded( channelTree );
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Got error from server", t );
            }
        } );

    }

    public void selectDomain( String domainId ) {
        for( Domain d : channelTree ) {
            if( d.getId().equals( domainId ) ) {
                publicChannels = new ArrayList<>();
                privateChannels = new ArrayList<>();

                String domainName = d.getName();
                for( Group g : d.getGroups() ) {
                    String groupName = g.getName();
                    for( Channel c : g.getChannels() ) {
                        ChannelEntry e = new ChannelEntry( c.getId(), domainName, groupName, c.getName(), c.isPrivateChannel() );
                        if( e.isPrivateChannel() ) {
                            privateChannels.add( e );
                        }
                        else {
                            publicChannels.add( e );
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder( View itemView ) {
            super( itemView );
        }
    }

    public class HeaderViewHolder extends ViewHolder
    {
        private TextView titleView;

        public HeaderViewHolder( View view ) {
            super( view );
            this.titleView = (TextView)view.findViewById( R.id.header_title_text );
            Log.i( "Created header view. titleView=" + titleView );
        }

        public void setTitle( String title ) {
            titleView.setText( title );
        }
    }

    public class ChannelViewHolder extends ViewHolder
    {
        private View view;
        private TextView contentView;

        public ChannelViewHolder( View view ) {
            super( view );
            this.view = view;
            contentView = (TextView)view.findViewById( R.id.content );
        }

        public void fillInChannelEntry( final ChannelEntry item ) {
            contentView.setText( item.getName() );

            view.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v ) {
                    if( parent.isTwoPane() ) {
                        parent.setActiveChannel( item.getId() );

                        Bundle arguments = new Bundle();
                        arguments.putString( ChannelContentFragment.ARG_CHANNEL_ID, item.getId() );
                        arguments.putString( ChannelContentFragment.ARG_CHANNEL_NAME, item.getName() );
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
                        intent.putExtra( ChannelContentFragment.ARG_CHANNEL_ID, item.getId() );
                        intent.putExtra( ChannelContentFragment.ARG_CHANNEL_NAME, item.getName() );
                        context.startActivity( intent );
                    }
                }
            } );
        }
    }
}
