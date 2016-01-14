package com.dhsdevelopments.potato;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.clientapi.Domain;
import com.dhsdevelopments.potato.clientapi.PotatoApi;
import com.dhsdevelopments.potato.dummy.DummyContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.*;

import java.util.List;

/**
 * An activity representing a list of Channels. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ChannelDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ChannelListActivity extends AppCompatActivity
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_channel_list );

        Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        FloatingActionButton fab = (FloatingActionButton)findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view ) {
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();
            }
        } );

        View recyclerView = findViewById( R.id.channel_list );
        assert recyclerView != null;
        setupRecyclerView( (RecyclerView)recyclerView );

        if( findViewById( R.id.channel_detail_container ) != null ) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" ).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( "http://10.0.2.2:8080/api/1.0/" )
                .addConverterFactory( GsonConverterFactory.create( gson ) )
                .build();
        PotatoApi api = retrofit.create( PotatoApi.class );
        Call<List<Domain>> call = api.getChannels( PotatoApplication.getInstance( this ).getApiKey() );
        call.enqueue( new Callback<List<Domain>>()
        {
            @Override
            public void onResponse( Response<List<Domain>> response, Retrofit retrofit ) {
                Log.i( "Got response from server: " + response.body() );
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.i( "Got error from server", t );
            }
        } );
    }

    private void setupRecyclerView( @NonNull RecyclerView recyclerView ) {
        recyclerView.setAdapter( new SimpleItemRecyclerViewAdapter( DummyContent.ITEMS ) );
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>
    {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter( List<DummyContent.DummyItem> items ) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
            View view = LayoutInflater.from( parent.getContext() )
                                      .inflate( R.layout.channel_list_content, parent, false );
            return new ViewHolder( view );
        }

        @Override
        public void onBindViewHolder( final ViewHolder holder, int position ) {
            holder.item = mValues.get( position );
            holder.idView.setText( mValues.get( position ).id );
            holder.contentView.setText( mValues.get( position ).content );

            holder.view.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v ) {
                    if( twoPane ) {
                        Bundle arguments = new Bundle();
                        arguments.putString( ChannelDetailFragment.ARG_ITEM_ID, holder.item.id );
                        ChannelDetailFragment fragment = new ChannelDetailFragment();
                        fragment.setArguments( arguments );
                        getSupportFragmentManager().beginTransaction()
                                                   .replace( R.id.channel_detail_container, fragment )
                                                   .commit();
                    }
                    else {
                        Context context = v.getContext();
                        Intent intent = new Intent( context, ChannelDetailActivity.class );
                        intent.putExtra( ChannelDetailFragment.ARG_ITEM_ID, holder.item.id );

                        context.startActivity( intent );
                    }
                }
            } );
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public final View view;
            public final TextView idView;
            public final TextView contentView;
            public DummyContent.DummyItem item;

            public ViewHolder( View view ) {
                super( view );
                this.view = view;
                idView = (TextView)view.findViewById( R.id.id );
                contentView = (TextView)view.findViewById( R.id.content );
            }

            @Override
            public String toString() {
                return super.toString() + " '" + contentView.getText() + "'";
            }
        }
    }
}
