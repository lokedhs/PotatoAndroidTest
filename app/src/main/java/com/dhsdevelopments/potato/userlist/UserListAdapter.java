package com.dhsdevelopments.potato.userlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult;
import com.dhsdevelopments.potato.clientapi.users.User;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.text.Collator;
import java.util.*;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder>
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_USER = 1;

    private Context context;
    private String cid;
    private ChannelUsersTracker userTracker;

    private List<UserWrapper> activeUsers = new ArrayList<>();
    private List<UserWrapper> inactiveUsers = new ArrayList<>();

    private ChannelUsersTracker.UserActivityListener listener;

    public UserListAdapter( Context context, String cid, ChannelUsersTracker userTracker ) {
        this.context = context;
        this.cid = cid;
        this.userTracker = userTracker;
        loadStateFromUserTracker();
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
        listener = new ActivityListener();
        userTracker.addUserActivityListener( listener );
    }

    @Override
    public void onDetachedFromRecyclerView( RecyclerView recyclerView ) {
        userTracker.removeUserActivityListener( listener );
        super.onDetachedFromRecyclerView( recyclerView );
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch( viewType ) {
            case VIEW_TYPE_HEADER:
                return new HeaderViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.user_list_header, parent, false ) );
            case VIEW_TYPE_USER:
                return new UserElementViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.user_list_element, parent, false ) );
            default:
                throw new IllegalArgumentException( "Unexpected viewType: " + viewType );
        }
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        if( position == 0 ) {
            ((HeaderViewHolder)holder).setHeaderTitle( "Active" );
        }
        else if( position == activeUsers.size() + 1 ) {
            ((HeaderViewHolder)holder).setHeaderTitle( "Inactive" );
        }
        else {
            int activeLength = activeUsers.size();
            UserWrapper user;
            if( position <= activeLength ) {
                user = activeUsers.get( position - 1 );
            }
            else {
                user = inactiveUsers.get( position - activeLength - 2 );
            }
            ((UserElementViewHolder)holder).fillInUser( user );
        }
    }

    @Override
    public int getItemCount() {
        return activeUsers.size() + inactiveUsers.size() + 2;
    }

    @Override
    public int getItemViewType( int position ) {
        return position == 0 || position == activeUsers.size() + 1 ? VIEW_TYPE_HEADER : VIEW_TYPE_USER;
    }

    private void loadStateFromUserTracker() {
        activeUsers.clear();
        inactiveUsers.clear();
        for( Map.Entry<String, ChannelUsersTracker.UserDescriptor> e : userTracker.getUsers().entrySet() ) {
            String uid = e.getKey();
            ChannelUsersTracker.UserDescriptor d = e.getValue();
            UserWrapper u = new UserWrapper( uid, d.getName() );
            if( d.isActive() ) {
                activeUsers.add( u );
            }
            else {
                inactiveUsers.add( u );
            }
        }

        final Collator collator = Collator.getInstance();
        Comparator<? super UserWrapper> comparator = new Comparator<UserWrapper>()
        {
            @Override
            public int compare( UserWrapper o1, UserWrapper o2 ) {
                return collator.compare( o1.getName(), o2.getName() );
            }
        };
        Collections.sort( activeUsers, comparator);
        Collections.sort( inactiveUsers, comparator );
        notifyDataSetChanged();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder( View itemView ) {
            super( itemView );
        }
    }

    private class HeaderViewHolder extends ViewHolder
    {
        private final TextView headerText;

        public HeaderViewHolder( View view ) {
            super( view);
            headerText = (TextView)view.findViewById( R.id.header_text );
        }

        public void setHeaderTitle( String title ) {
            headerText.setText( title );
        }
    }

    private class UserElementViewHolder extends ViewHolder
    {
        private UserWrapper user;
        private TextView userDescriptionView;

        public UserElementViewHolder( View itemView ) {
            super( itemView );
            userDescriptionView = (TextView)itemView.findViewById( R.id.user_description_view );
        }

        public void fillInUser( UserWrapper user ) {
            this.user = user;
            userDescriptionView.setText( user.getName() );
        }
    }

    private class ActivityListener implements ChannelUsersTracker.UserActivityListener
    {

        @Override
        public void activeUserListSync() {
            loadStateFromUserTracker();
        }

        @Override
        public void userUpdated( String uid ) {
            loadStateFromUserTracker();
        }
    }
}
