package com.dhsdevelopments.potato.editor;

import android.widget.MultiAutoCompleteTextView;
import com.dhsdevelopments.potato.Log;

public class UserNameTokeniser implements MultiAutoCompleteTextView.Tokenizer
{
    @Override
    public int findTokenStart( CharSequence text, int cursor ) {
        if( cursor == 0 ) {
            return cursor;
        }

        int w = Character.offsetByCodePoints( text, cursor, -1 );
        while( true ) {
            int codePoint = Character.codePointAt( text, w );
            if( codePoint == '@' && (w == 0 || Character.isSpaceChar( Character.codePointBefore( text, w ) )) ) {
                return Character.offsetByCodePoints( text, w, 1 );
            }
            else if( !isTokenCharacter( codePoint ) ) {
                return cursor;
            }

            if( w == 0 ) {
                break;
            }

            w = Character.offsetByCodePoints( text, w, -1 );
        }

        Log.i( "Returning token start: " + cursor );
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
        Log.i( "Terminate token called: '" + text + "'" );
        return text;
    }
}
