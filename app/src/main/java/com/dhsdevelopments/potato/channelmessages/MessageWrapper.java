package com.dhsdevelopments.potato.channelmessages;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.message.MessageElement;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

public class MessageWrapper
{
    private String messageId;
    private String senderName;
    private Date createdDate;
    private String createdDateFormatted;
    private MessageElement content;

    public MessageWrapper( Message msg, DateFormat isoDateFormat, MessageFormat dateFormat ) {
        this.messageId = msg.id;
        this.senderName = msg.fromName;
        this.content = msg.text;

        Date date;
        try {
            date = isoDateFormat.parse( msg.createdDate );
        }
        catch( ParseException e ) {
            throw new IllegalStateException( "Unable to parse date format from server: '" + this.createdDate + "'", e );
        }
        this.createdDate = date;
        this.createdDateFormatted = dateFormat.format( new Object[] { date } );
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getCreatedDateFormatted() {
        return createdDateFormatted;
    }

    public MessageElement getContent() {
        return content;
    }

    public CharSequence getMarkupContent() {
        return content.getSpannable();
    }

    public void setContent( MessageElement content ) {
        this.content = content;
    }
}
