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
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker;
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

    private Context context;
    private String cid;

    private MessageFormat dateFormat;
    private SimpleDateFormat isoDateFormat;
    private ImageCache imageCache;

    private List<MessageWrapper> messages = new ArrayList<>();
    private ChannelUsersTracker usersTracker;

    public ChannelContentAdapter( Context context, ChannelUsersTracker usersTracker, String cid ) {
        this.context = context;
        this.usersTracker = usersTracker;
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
        for( Message m : messages ) {
            result.add( new MessageWrapper( m, isoDateFormat, dateFormat ) );
        }
        this.messages = result;
        notifyDataSetChanged();
        Log.i( "Updated " + this.messages.size() + " messages" );
    }

    public void loadMessages() {
        PotatoApplication app = PotatoApplication.getInstance( context );
        Call<MessageHistoryResult> call = app.getPotatoApi().loadHistoryAsJson( app.getApiKey(), cid, 10 );
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

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        switch( viewType ) {
            case VIEW_TYPE_PLAIN_MESSAGE:
                return new ViewHolder( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_basic, parent, false ) );
            case VIEW_TYPE_EXTRA_CONTENT:
                return new ViewHolderExtraContent( LayoutInflater.from( parent.getContext() ).inflate( R.layout.message_extra_html, parent, false ) );
            default:
                throw new IllegalStateException( "Unexpected viewType: " + viewType );
        }
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        holder.fillInView( messages.get( position ) );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType( int position ) {
        MessageWrapper m = messages.get( position );
        if( m.getExtraHtml() == null && m.getImage() == null ) {
            return VIEW_TYPE_PLAIN_MESSAGE;
        }
        else {
            return VIEW_TYPE_EXTRA_CONTENT;
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
                notifyItemInserted( insertionPos );
            }
        }
        else {
            // This is an update. Only update if the message is already in the log.
            if( pos >= 0 ) {
                if( msg.deleted ) {
                    messages.remove( pos );
                    notifyItemRemoved( pos );
                }
                else {
                    messages.set( pos, w );
                    notifyItemChanged( pos );
                }
            }
        }
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
                imageCache.loadImageFromApi( messageImage.file, 256, 256, StorageType.LONG, callback );
            }
            else {
                imageView.setImageDrawable( null );
                imageView.setVisibility( View.GONE );
            }
        }
    }
}
