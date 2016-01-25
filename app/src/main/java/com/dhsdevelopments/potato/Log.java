package com.dhsdevelopments.potato;

public class Log
{
    public static final String LOG_TAG = "potato";

    private Log() {
        // Prevent instantiation
    }

    public static void e( String message ) {
        android.util.Log.e( LOG_TAG, message );
    }

    public static void e( String message, Throwable e ) {
        android.util.Log.e( LOG_TAG, message, e );
    }

    public static void w( String message ) {
        android.util.Log.w( LOG_TAG, message );
    }

    public static void w( String message, Throwable e ) {
        android.util.Log.w( LOG_TAG, message, e );
    }

    public static void i( String message ) {
        android.util.Log.i( LOG_TAG, message );
    }

    public static void i( String message, Throwable e ) {
        android.util.Log.i( LOG_TAG, message, e );
    }

    public static void d( String message ) {
        android.util.Log.d( LOG_TAG, message );
    }

    public static void d( String message, Throwable e ) {
        android.util.Log.d( LOG_TAG, message, e );
    }

    public static void wtf( String message ) {
        android.util.Log.e( LOG_TAG, message );
        throw new RuntimeException( message );
    }

    public static void wtf( String message, Throwable e ) {
        android.util.Log.e( LOG_TAG, message, e );
        throw new RuntimeException( message, e );
    }
}
