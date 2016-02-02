package com.dhsdevelopments.potato.channelmessages;

import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.message.MessageElement;
import com.dhsdevelopments.potato.clientapi.message.MessageImage;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

public class MessageWrapper
{
    private Message msg;
    private String senderImageName;
    private Date createdDate;
    private String createdDateFormatted;

    public MessageWrapper( Message msg, String senderImageName, DateFormat isoDateFormat, MessageFormat dateFormat ) {
        this.msg = msg;
        this.senderImageName = senderImageName;

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

    public String getSender() {
        return msg.from;
    }

    public String getSenderName() {
        return msg.fromName;
    }

    public String getSenderImageName() {
        return senderImageName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getCreatedDateFormatted() {
        return createdDateFormatted;
    }

    public MessageElement getContent() {
        return msg.text;
    }

    public CharSequence getMarkupContent() {
        return msg.text.getSpannable();
    }

    public String getExtraHtml() {
        return msg.extraHtml;
    }

    public MessageImage getImage() {
        return msg.messageImage;
    }
}
