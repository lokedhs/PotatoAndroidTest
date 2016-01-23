package com.dhsdevelopments.potato.editor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.widget.Filter;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

import java.text.Collator;
import java.util.*;

class UserNameSuggestFilter extends Filter
{
    private Context context;
    private ChannelUsersTracker usersTracker;
    private UserNameSuggestAdapter userNameSuggestAdapter;

    private UserTrackerListener listener;
    private List<UserSuggestion> users = null;
    private Comparator<UserSuggestion> userSuggestionComparator;

    public UserNameSuggestFilter( Context context, ChannelUsersTracker usersTracker, UserNameSuggestAdapter userNameSuggestAdapter ) {
        this.context = context;
        this.usersTracker = usersTracker;
        this.userNameSuggestAdapter = userNameSuggestAdapter;

        final Collator collator = Collator.getInstance();
        userSuggestionComparator = new Comparator<UserSuggestion>()
        {
            @Override
            public int compare( UserSuggestion o1, UserSuggestion o2 ) {
                return collator.compare( o1.getName(), o2.getName() );
            }
        };

        listener = new UserTrackerListener();
        usersTracker.addUserActivityListener( listener );
    }

    @Override
    protected FilterResults performFiltering( CharSequence text ) {
        if( text == null ) {
            return null;
        }

        String textLower = text.toString().toLowerCase();
        List<UserSuggestion> result = new ArrayList<>();
        for( UserSuggestion s : getUsers() ) {
            if( s.getName().toLowerCase().startsWith( textLower ) ) {
                result.add( s );
            }
        }

        FilterResults filterResults = new FilterResults();
        filterResults.values = result;
        filterResults.count = result.size();
        return filterResults;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void publishResults( CharSequence charSequence, FilterResults filterResults ) {
        if( filterResults != null ) {
            userNameSuggestAdapter.setSuggestionList( (List<UserSuggestion>)filterResults.values );
        }
    }

    private synchronized List<UserSuggestion> getUsers() {
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

    private synchronized void clearUserList() {
        users = null;
    }

    public void shutdown() {
        usersTracker.removeUserActivityListener( listener );
    }

    private class UserTrackerListener implements ChannelUsersTracker.UserActivityListener
    {
        @Override
        public void activeUserListSync() {
            clearUserList();
        }

        @Override
        public void userUpdated( String uid ) {
            clearUserList();
        }
    }
}
