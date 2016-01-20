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
import com.dhsdevelopments.potato.clientapi.users.LoadUsersResult;
import com.dhsdevelopments.potato.clientapi.users.User;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder>
{
    private Context context;
    private String cid;

    private List<UserWrapper> activeUsers = new ArrayList<>();
    private List<UserWrapper> inactiveUsers = new ArrayList<>();

    public UserListAdapter( Context context, String cid ) {
        this.context = context;
        this.cid = cid;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.user_list_element, parent, false );
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        int activeLength = activeUsers.size();
        UserWrapper user;
        if( position < activeLength ) {
            user = activeUsers.get( position );
        }
        else {
            user = inactiveUsers.get( position - activeLength );
        }
        holder.fillInUser( user );
    }

    @Override
    public int getItemCount() {
        return activeUsers.size() + inactiveUsers.size();
    }

    public void loadUsers() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<LoadUsersResult> call = app.getPotatoApi().loadUsers( app.getApiKey(), cid );
        call.enqueue( new Callback<LoadUsersResult>()
        {
            @Override
            public void onResponse( Response<LoadUsersResult> response, Retrofit retrofit ) {
                if( response.isSuccess() ) {
                    updateUsers( response.body().members );
                }
                else {
                    Log.wtf( "Error code from server" );
                }
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Error loading users", t );
            }
        } );
    }

    private void updateUsers( List<User> users ) {
        for( User u : users ) {
            String uid = u.id;
            UserWrapper w = findUserFromList( uid, activeUsers );
            if( w == null ) {
                w = findUserFromList( uid, inactiveUsers );
            }
            if( w != null ) {
                w.fillInFromUser( u );
            }
            else {
                inactiveUsers.add( new UserWrapper( u ) );
            }
        }
        notifyDataSetChanged();
    }

    private UserWrapper findUserFromList( String uid, List<UserWrapper> users ) {
        for( UserWrapper w : users ) {
            if( w.getId().equals( uid ) ) {
                return w;
            }
        }
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private UserWrapper user;
        private TextView userDescriptionView;

        public ViewHolder( View itemView ) {
            super( itemView );
            userDescriptionView = (TextView)itemView.findViewById( R.id.user_description_view );
        }

        public void fillInUser( UserWrapper user ) {
            this.user = user;
            userDescriptionView.setText( user.getName() );
        }
    }
}
