package com.dhsdevelopments.potato.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

import java.util.List;

public class UserNameSuggestAdapter extends BaseAdapter implements Filterable
{
    private LayoutInflater inflater;
    private List<UserSuggestion> users = null;
    private UserNameSuggestFilter filter;

    public UserNameSuggestAdapter( Context context, ChannelUsersTracker usersTracker ) {
        filter = new UserNameSuggestFilter( usersTracker, this );
        inflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }

    public void shutdown() {
        filter.shutdown();
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem( int position ) {
        return users.get( position ).getId();
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View v;
        if( convertView != null && convertView.getId() == R.id.user_name_suggest_line_view ) {
            v = convertView;
        }
        else {
            v = inflater.inflate( R.layout.user_name_suggest_line, parent, false );
        }

        UserSuggestion userSuggestion = users.get( position );
        TextView textView = (TextView)v.findViewById( R.id.user_name_suggest_name );
        textView.setText( userSuggestion.getName() );
        return v;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    void setSuggestionList( List<UserSuggestion> users) {
        this.users = users;
        Log.i( "Setting suggestion list: " + users );
        notifyDataSetChanged();
    }
}
