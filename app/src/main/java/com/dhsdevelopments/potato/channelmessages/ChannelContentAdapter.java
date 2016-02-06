package com.dhsdevelopments.potato.channelmessages;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dhsdevelopments.potato.Log;
import com.dhsdevelopments.potato.PotatoApplication;
import com.dhsdevelopments.potato.R;
import com.dhsdevelopments.potato.clientapi.message.Message;
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult;
import com.dhsdevelopments.potato.clientapi.message.MessageImage;
import com.dhsdevelopments.potato.imagecache.ImageCache;
import com.dhsdevelopments.potato.imagecache.LoadImageCallback;
import com.dhsdevelopments.potato.imagecache.StorageType;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChannelContentAdapter extends RecyclerView.Adapter<ChannelContentAdapter.ViewHolder>
{
    private static final int VIEW_TYPE_PLAIN_MESSAGE = 0;
    private static final int VIEW_TYPE_EXTRA_CONTENT = 1;
    private static final int VIEW_TYPE_END_OF_CHANNEL_MARKER = 2;

    private static final int NUM_MESSAGES_PER_LOAD = 20;

    private Context context;
    private String cid;

    private MessageFormat dateFormat;
    private SimpleDateFormat isoDateFormat;
    private ImageCache imageCache;

    private List<MessageWrapper> messages = new ArrayList<>();

    public ChannelContentAdapter( Context context, String cid ) {
        this.context = context;
        this.cid = cid;

        dateFormat = new MessageFormat( context.getResources().getString( R.string.message_entry_date_label ) );
        isoDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        isoDateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
        imageCache = new ImageCache( context );
    }

    @Override
    public void onDetachedFromRecyclerView( RecyclerView recyclerView ) {
        imageCache.close();
        super.onDetachedFromRecyclerView( recyclerView );
    }

    public void setMessages( List<Message> messages ) {
        List<MessageWrapper> result = new ArrayList<>( messages.size() );
        int i = 0;
        for( Message m : messages ) {
            if( !m.deleted ) {
                MessageWrapper msg = new MessageWrapper( m, isoDateFormat, dateFormat );
                result.add( msg );
                if( i > 0 && shouldHideHeader( result.get( i - 1 ), msg ) ) {
                    msg.setShouldDisplayHeader( false );
                }
                i++;
            }
        }
        this.messages = result;
        notifyDataSetChanged();
    }

    public void loadMessages() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<MessageHistoryResult> call = app.getPotatoApi().loadHistoryAsJson( app.getApiKey(), cid, NUM_MESSAGES_PER_LOAD );
        call.enqueue( new Callback<MessageHistoryResult>()
        {
            @Override
            public void onResponse( Response<MessageHistoryResult> response, Retrofit retrofit ) {
                setMessages( response.body().getMessages() );
            }

            @Override
            public void onFailure( Throwable t ) {
                Log.wtf( "Failed to load message history", t );
            }
        } );
    }

    public void loadMoreMessages() {
        Log.w( "Need to load here" );
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch( viewType ) {
            case VIEW_TYPE_PLAIN_MESSAGE:
                return new ViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_basic, parent, false ) );
            case VIEW_TYPE_EXTRA_CONTENT:
                return new ViewHolderExtraContent( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_extra_html, parent, false ) );
            case VIEW_TYPE_END_OF_CHANNEL_MARKER:
                return new EndOfChannelMarkerViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_marker, parent, false ) );
            default:
                throw new IllegalStateException( "Unexpected viewType: " + viewType );
        }
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        if( position < messages.size() ) {
            holder.fillInView( messages.get( position ) );
        }
    }

    @Override
    public int getItemCount() {
        return messages.size() + 1;
    }

    @Override
    public int getItemViewType( int position ) {
        if( position == messages.size() ) {
            return VIEW_TYPE_END_OF_CHANNEL_MARKER;
        }
        else {
            MessageWrapper m = messages.get( position );
            if( m.getExtraHtml() == null && m.getImage() == null ) {
                return VIEW_TYPE_PLAIN_MESSAGE;
            }
            else {
                return VIEW_TYPE_EXTRA_CONTENT;
            }
        }
    }

    /**
     * Called when a new message is received from the server.
     */
    public void newMessage( Message msg ) {
        MessageWrapper w = new MessageWrapper( msg, isoDateFormat, dateFormat );
        int pos = Collections.binarySearch( messages, w, new Comparator<MessageWrapper>()
        {
            @Override
            public int compare( MessageWrapper m1, MessageWrapper m2 ) {
                return m1.getCreatedDate().compareTo( m2.getCreatedDate() );
            }
        } );

        if( msg.updated == null ) {
            // This is a new message, add it at the appropriate position.
            // Don't do this if the message is already in the list.
            if( pos < 0 ) {
                int insertionPos = -pos - 1;
                messages.add( insertionPos, w );
                updateDisplayStateForPosition( insertionPos );
                notifyItemInserted( insertionPos );

                int nextPos = insertionPos + 1;
                if( nextPos < messages.size() && updateDisplayStateForPosition( insertionPos + 1 ) ) {
                    notifyItemChanged( pos );
                }
            }
        }
        else {
            // This is an update. Only update if the message is already in the log.
            if( pos >= 0 ) {
                if( msg.deleted ) {
                    messages.remove( pos );
                    notifyItemRemoved( pos );
                    if( updateDisplayStateForPosition( pos ) ) {
                        notifyItemChanged( pos );
                    }
                }
                else {
                    messages.set( pos, w );
                    notifyItemChanged( pos );
                }
            }
        }
    }

    boolean shouldHideHeader( MessageWrapper reference, MessageWrapper msg ) {
        if( !reference.getSender().equals( msg.getSender() ) ) {
            return false;
        }

        // Only collapse messages that are sent within 1 minute of each other
        return msg.getCreatedDate().getTime() - reference.getCreatedDate().getTime() < 60 * 1000;
    }

    boolean updateDisplayStateForPosition( int pos ) {
        if( pos < messages.size() ) {
            MessageWrapper msg = messages.get( pos );
            boolean shouldDisplay = (pos == 0 || !shouldHideHeader( messages.get( pos - 1 ), msg ));
            if( (shouldDisplay && !msg.isShouldDisplayHeader())
                        || (!shouldDisplay && msg.isShouldDisplayHeader()) ) {
                msg.setShouldDisplayHeader( shouldDisplay );
                return true;
            }
        }
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView senderView;
        private TextView dateView;
        private TextView contentView;
        private ImageView imageView;
        private long updateIndex = 0;

        public ViewHolder( View itemView ) {
            super( itemView );

            senderView = (TextView)itemView.findViewById( R.id.sender );
            dateView = (TextView)itemView.findViewById( R.id.date );
            contentView = (TextView)itemView.findViewById( R.id.content );
            imageView = (ImageView)itemView.findViewById( R.id.image );
        }

        public void fillInView( MessageWrapper message ) {
            senderView.setText( message.getSenderName() );
            dateView.setText( message.getCreatedDateFormatted() );
            contentView.setText( message.getMarkupContent() );

            int dh = message.isShouldDisplayHeader() ? View.VISIBLE : View.GONE;
            senderView.setVisibility( dh );
            dateView.setVisibility( dh );
            imageView.setVisibility( dh );

            final Resources resources = context.getResources();
            int imageWidth = resources.getDimensionPixelSize( R.dimen.chat_image_width );
            int imageHeight = resources.getDimensionPixelSize( R.dimen.chat_image_height );

            updateIndex++;
            final long oldUpdateIndex = updateIndex;
            imageCache.loadImageFromApi( "/users/" + message.getSender() + "/image", imageWidth, imageHeight, StorageType.LONG,
                                         new LoadImageCallback()
                                         {
                                             @Override
                                             public void bitmapLoaded( Bitmap bitmap ) {
                                                 if( updateIndex == oldUpdateIndex ) {
                                                     imageView.setImageDrawable( new BitmapDrawable( resources, bitmap ) );
                                                 }
                                             }

                                             @Override
                                             public void bitmapNotFound() {
                                                 if( updateIndex == oldUpdateIndex ) {
                                                     imageView.setImageDrawable( new ColorDrawable( Color.GREEN ) );
                                                 }
                                             }
                                         } );
        }
    }

    public class ViewHolderExtraContent extends ViewHolder
    {
        private ImageView imageView;
        private TextView htmlContentView;
        private long imageLoadIndex = 0;

        public ViewHolderExtraContent( View itemView ) {
            super( itemView );
            imageView = (ImageView)itemView.findViewById( R.id.message_image );
            htmlContentView = (TextView)itemView.findViewById( R.id.extra_content_html );
        }

        @Override
        public void fillInView( MessageWrapper message ) {
            super.fillInView( message );

            if( message.getExtraHtml() != null ) {
                htmlContentView.setText( Html.fromHtml( message.getExtraHtml() ) );
                htmlContentView.setVisibility( View.VISIBLE );
            }
            else {
                htmlContentView.setText( "" );
                htmlContentView.setVisibility( View.GONE );
            }

            MessageImage messageImage = message.getImage();
            final long refIndex = ++imageLoadIndex;
            if( messageImage != null ) {
                imageView.setVisibility( View.GONE );
                LoadImageCallback callback = new LoadImageCallback()
                {
                    @Override
                    public void bitmapLoaded( Bitmap bitmap ) {
                        if( imageLoadIndex == refIndex ) {
                            imageView.setImageDrawable( new BitmapDrawable( context.getResources(), bitmap ) );
                            imageView.setVisibility( View.VISIBLE );
                        }
                    }

                    @Override
                    public void bitmapNotFound() {
                        // Do nothing
                    }
                };
                Resources res = context.getResources();
                int imageWidth = res.getDimensionPixelSize( R.dimen.message_image_width );
                int imageHeight = res.getDimensionPixelSize( R.dimen.message_image_height );
                imageCache.loadImageFromApi( messageImage.file, imageWidth, imageHeight, StorageType.LONG, callback );
            }
            else {
                imageView.setImageDrawable( null );
                imageView.setVisibility( View.GONE );
            }
        }
    }

    private class EndOfChannelMarkerViewHolder extends ViewHolder
    {
        public EndOfChannelMarkerViewHolder( View view ) {
            super( view );
        }
    }
}
