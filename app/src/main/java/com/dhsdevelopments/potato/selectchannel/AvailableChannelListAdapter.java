package com.dhsdevelopments.potato.selectchannel;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.R;

import java.util.Collections;
import java.util.List;

public class AvailableChannelListAdapter extends RecyclerView.Adapter<AvailableChannelListAdapter.ViewHolder>
{
    private List<AvailableChannel> channels = Collections.emptyList();

    public AvailableChannelListAdapter( String domainId ) {

    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        return null;
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView titleView;

        public ViewHolder( View view ) {
            super( view );
            this.titleView = (TextView)view.findViewById( R.id.header_title_text );
            Log.i( "Created header view. titleView=" + titleView );
        }

        public void setTitle( String title ) {
            titleView.setText( title );
        }
    }

}
