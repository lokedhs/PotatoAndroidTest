package com.dhsdevelopments.potato.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

import java.text.Collator;
import java.util.*;

public class UserNameSuggestAdapter extends BaseAdapter implements Filterable
{
    private UserTrackerListener listener;
    private LayoutInflater inflater;
    private ChannelUsersTracker usersTracker;
    private List<UserSuggestion> users = null;
    private Comparator<UserSuggestion> userSuggestionComparator;

    public UserNameSuggestAdapter( Context context, ChannelUsersTracker usersTracker ) {
        this.usersTracker = usersTracker;
        inflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        listener = new UserTrackerListener();

        final Collator collator = Collator.getInstance();
        userSuggestionComparator = new Comparator<UserSuggestion>()
        {
            @Override
            public int compare( UserSuggestion o1, UserSuggestion o2 ) {
                return collator.compare( o1.name, o2.name );
            }
        };

        usersTracker.addUserActivityListener( listener );
    }

    @Override
    public int getCount() {
        return getUserSuggestions().size();
    }

    @Override
    public Object getItem( int position ) {
        return getUserSuggestions().get( position );
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

        UserSuggestion userSuggestion = getUserSuggestions().get( position );
        TextView textView = (TextView)v.findViewById( R.id.user_name_suggest_name );
        textView.setText( userSuggestion.name );
        return v;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    private List<UserSuggestion> getUserSuggestions() {
        if( users == null ) {
            Map<String, ChannelUsersTracker.UserDescriptor> userlist = usersTracker.getUsers();
            users = new ArrayList<>( userlist.size() );
            for( Map.Entry<String, ChannelUsersTracker.UserDescriptor> u : userlist.entrySet() ) {
                users.add( new UserSuggestion( u.getKey(), u.getValue().getName() ) );
            }
            Collections.sort( users, userSuggestionComparator );
        }
        return users;
    }

    private void clearAndNotify() {
        users = null;
        notifyDataSetChanged();
    }

    private class UserTrackerListener implements ChannelUsersTracker.UserActivityListener
    {
        @Override
        public void activeUserListSync() {
            clearAndNotify();
        }

        @Override
        public void userUpdated( String uid ) {
            clearAndNotify();
        }
    }

    private static class UserSuggestion
    {
        private String id;
        private String name;

        public UserSuggestion( String id, String name ) {
            this.id = id;
            this.name = name;
        }
    }
}
