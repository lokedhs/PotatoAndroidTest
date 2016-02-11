package com.dhsdevelopments.potato.editor;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.widget.MultiAutoCompleteTextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;

public class UserNameTokeniser implements MultiAutoCompleteTextView.Tokenizer
{
    private ChannelUsersTracker userTracker;

    public UserNameTokeniser( ChannelUsersTracker userTracker ) {
        this.userTracker = userTracker;
    }

    @Override
    public int findTokenStart( CharSequence text, int cursor ) {
        if( cursor == 0 ) {
            return cursor;
        }

        int w = Character.offsetByCodePoints( text, cursor, -1 );
        while( true ) {
            int codePoint = Character.codePointAt( text, w );
            if( codePoint == '@' && (w == 0 || Character.isSpaceChar( Character.codePointBefore( text, w ) )) ) {
                return w;
            }
            else if( !isTokenCharacter( codePoint ) ) {
                return cursor;
            }

            if( w == 0 ) {
                break;
            }

            w = Character.offsetByCodePoints( text, w, -1 );
        }

        return cursor;
    }

    private boolean isTokenCharacter( int codePoint ) {
        return Character.isLetterOrDigit( codePoint )
                       || codePoint == '.'
                       || codePoint == '_'
                       || codePoint == '@';
    }

    @Override
    public int findTokenEnd( CharSequence text, int cursor ) {
        return cursor;
    }

    @Override
    public CharSequence terminateToken( CharSequence text ) {
        Log.INSTANCE.i( "Terminate token called: '" + text + "', type: " + text.getClass().getName() );
        String uid = text.toString();
        ChannelUsersTracker.UserDescriptor u = userTracker.getUsers().get( uid );
        String name = u == null ? uid : u.getName();

        Spannable s = new SpannableString( name );
        s.setSpan( new BackgroundColorSpan( Color.rgb( 210, 210, 210 ) ), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        s.setSpan( new UidSpan( uid, name ), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        return s;
    }
}
