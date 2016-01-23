package com.dhsdevelopments.potato.editor;

import android.widget.MultiAutoCompleteTextView;

public class UserNameTokeniser implements MultiAutoCompleteTextView.Tokenizer
{
    @Override
    public int findTokenStart( CharSequence text, int cursor ) {
        for( int i = cursor - 1 ; i >= 0 ; i-- ) {
            if( text.charAt( i ) == '@' ) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int findTokenEnd( CharSequence text, int cursor ) {
        return cursor;
    }

    @Override
    public CharSequence terminateToken( CharSequence text ) {
        return text;
    }
}
