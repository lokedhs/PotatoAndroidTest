package com.dhsdevelopments.potato.service;

import java.io.InterruptedIOException;

public class ReceiverStoppedException extends Exception
{
    public ReceiverStoppedException() {
    }

    public ReceiverStoppedException( InterruptedIOException e ) {
        super( e );
    }
}
