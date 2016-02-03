package com.dhsdevelopments.potato.selectchannel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import com.dhsdevelopments.potato.R;

public class SelectChannelActivity extends AppCompatActivity
{
    public static final String EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_select_channel );
        setTitle( "Available channels" );

        String domainId = getIntent().getStringExtra( EXTRA_DOMAIN_ID );

        RecyclerView recyclerView = (RecyclerView)findViewById( R.id.channel_select_list );
        recyclerView.setAdapter( new AvailableChannelListAdapter( domainId ) );
    }
}
