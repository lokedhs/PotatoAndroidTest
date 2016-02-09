package com.dhsdevelopments.potato.channelmessages

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.LoadImageCallback
import com.dhsdevelopments.potato.imagecache.StorageType
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class ChannelContentAdapter(private val context: Context, private val cid: String) :
        RecyclerView.Adapter<ChannelContentAdapter.ViewHolder>() {
    companion object {
        private val VIEW_TYPE_PLAIN_MESSAGE = 0
        private val VIEW_TYPE_EXTRA_CONTENT = 1
        private val VIEW_TYPE_END_OF_CHANNEL_MARKER = 2

        private val NUM_MESSAGES_PER_LOAD = 20

        val EXTRA_MESSAGE_ID = "com.dhsdevelopments.potato.message_id"
        val EXTRA_MESSAGE_CONTEXT_ACTION = "com.dhsdevelopments.potato.message_context_action"
        val MESSAGE_CONTEXT_ACTION_DELETE_MESSAGE = "deleteMessage"
    }

    private val dateFormat: MessageFormat
    private val isoDateFormat: SimpleDateFormat
    private var imageCache: ImageCache? = null

    private var messages: MutableList<MessageWrapper> = ArrayList()
    private var isLoading = false

    val elementDecoration: RecyclerView.ItemDecoration by lazy {
        MessageElementItemDecoration()
    }

    init {
        dateFormat = MessageFormat(context.resources.getString(R.string.message_entry_date_label))
        isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        isoDateFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        imageCache = ImageCache(context)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        imageCache!!.close()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    //    fun addItemSelectionListener(listener: (String) -> Unit) {
    //        itemSelectionListeners.add(listener)
    //    }

    fun parseMessageList(messages: List<Message>): List<MessageWrapper> {
        val result = ArrayList<MessageWrapper>(messages.size)
        var i = 0
        for (m in messages) {
            if (!m.deleted) {
                val msg = MessageWrapper(m, isoDateFormat, dateFormat)
                result.add(msg)
                if (i > 0 && shouldHideHeader(result[i - 1], msg)) {
                    msg.isShouldDisplayHeader = false
                }
                i++
            }
        }
        return result
    }

    fun loadMessageHistory(startMessageId: String?, callback: (List<Message>) -> Unit, errorCallback: (String) -> Unit) {
        if (isLoading) {
            Log.w("Attempt to load messages while loading is in progress")
            return
        }

        val handler = Handler()

        val app = PotatoApplication.getInstance(context)
        val call = app.potatoApi.loadHistoryAsJson(app.apiKey, cid, NUM_MESSAGES_PER_LOAD, startMessageId ?: "now")
        isLoading = true
        call.enqueue(object : Callback<MessageHistoryResult> {
            override fun onResponse(response: Response<MessageHistoryResult>, retrofit: Retrofit) {
                handler.post {
                    isLoading = false
                    if (response.isSuccess) {
                        callback(response.body().messages)
                    }
                    else {
                        errorCallback("HTTP error: " + response.code())
                    }
                }
            }

            override fun onFailure(t: Throwable) {
                handler.post {
                    isLoading = false
                    errorCallback(t.message ?: "Unknown error")
                }
            }
        })
    }

    fun loadMessages(callback: LoadMessagesCallback) {
        loadMessageHistory(null, {
            val msgs = parseMessageList(it)

            messages.clear();
            messages.addAll(msgs)

            notifyDataSetChanged()

            callback.loadSuccessful(msgs)
        }, { callback.loadFailed(it) })
    }

    fun loadMoreMessages(callback: LoadMessagesCallback) {
        if (messages.isEmpty()) {
            loadMessages(callback)
        }
        else {
            val lastMessageId = messages[0].id
            loadMessageHistory(lastMessageId, {
                val parsed = parseMessageList(it)
                messages.addAll(0, parsed)
                notifyItemRangeInserted(0, parsed.size)
                callback.loadSuccessful(parsed)
            }, { callback.loadFailed(it) })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PLAIN_MESSAGE -> MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_basic, parent, false))
            VIEW_TYPE_EXTRA_CONTENT -> MessageViewHolderExtraContent(LayoutInflater.from(parent.context).inflate(R.layout.message_extra_html, parent, false))
            VIEW_TYPE_END_OF_CHANNEL_MARKER -> EndOfChannelMarkerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_marker, parent, false))
            else -> throw IllegalStateException("Unexpected viewType: " + viewType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < messages.size) {
            (holder as MessageViewHolder).fillInView(messages[position])
        }
    }

    fun positionForMessage(messageId: String): Int {
        var i = 0;
        for (msg in messages) {
            if (msg.id == messageId) {
                return i;
            }
            i++;
        }
        throw IllegalStateException("Message with id: $messageId not found")
    }

    override fun getItemCount(): Int {
        return messages.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == messages.size) {
            return VIEW_TYPE_END_OF_CHANNEL_MARKER
        }
        else {
            val m = messages[position]
            if (m.extraHtml == null && m.image == null) {
                return VIEW_TYPE_PLAIN_MESSAGE
            }
            else {
                return VIEW_TYPE_EXTRA_CONTENT
            }
        }
    }

    /**
     * Called when a new message is received from the server.
     */
    fun newMessage(msg: Message) {
        val w = MessageWrapper(msg, isoDateFormat, dateFormat)
        val pos = Collections.binarySearch(messages, w) { m1, m2 -> m1.createdDate.compareTo(m2.createdDate) }

        if (msg.updated == null) {
            // This is a new message, add it at the appropriate position.
            // Don't do this if the message is already in the list.
            if (pos < 0) {
                val insertionPos = -pos - 1
                messages.add(insertionPos, w)
                updateDisplayStateForPosition(insertionPos)
                notifyItemInserted(insertionPos)

                val nextPos = insertionPos + 1
                if (nextPos < messages.size && updateDisplayStateForPosition(insertionPos + 1)) {
                    notifyItemChanged(pos)
                }
            }
        }
        else {
            // This is an update. Only update if the message is already in the log.
            if (pos >= 0) {
                if (msg.deleted) {
                    messages.removeAt(pos)
                    notifyItemRemoved(pos)
                    if (updateDisplayStateForPosition(pos)) {
                        notifyItemChanged(pos)
                    }
                }
                else {
                    messages[pos] = w
                    notifyItemChanged(pos)
                }
            }
        }
    }

    internal fun shouldHideHeader(reference: MessageWrapper, msg: MessageWrapper): Boolean {
        if (reference.sender != msg.sender) {
            return false
        }

        // Only collapse messages that are sent within 1 minute of each other
        return msg.createdDate.time - reference.createdDate.time < 60 * 1000
    }

    internal fun updateDisplayStateForPosition(pos: Int): Boolean {
        if (pos < messages.size) {
            val msg = messages[pos]
            val shouldDisplay = pos == 0 || !shouldHideHeader(messages[pos - 1], msg)
            if (shouldDisplay && !msg.isShouldDisplayHeader || !shouldDisplay && msg.isShouldDisplayHeader) {
                msg.isShouldDisplayHeader = shouldDisplay
                return true
            }
        }
        return false
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    open inner class MessageViewHolder(itemView: View) : ViewHolder(itemView) {
        private val senderView: TextView
        private val dateView: TextView
        private val contentView: TextView
        private val imageView: ImageView
        private var updateIndex: Long = 0
        private var currentMessage: MessageWrapper? = null

        init {
            senderView = itemView.findViewById(R.id.sender) as TextView
            dateView = itemView.findViewById(R.id.date) as TextView
            contentView = itemView.findViewById(R.id.content) as TextView
            imageView = itemView.findViewById(R.id.image) as ImageView
            //itemView.isLongClickable = true
            itemView.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo -> buildContextMenu(contextMenu!!) }
            itemView.setOnContextClickListener { Log.d("Selected item: "); true }
        }

        private fun buildContextMenu(menu: ContextMenu) {
            val userId = PotatoApplication.getInstance(context).userId
            Log.d("checking sender ${currentMessage!!.sender} against local user $userId")
            if(currentMessage!!.sender == userId) {
                val item = menu.add("Delete message")
                val intent = Intent()
                intent.putExtra(EXTRA_MESSAGE_CONTEXT_ACTION, MESSAGE_CONTEXT_ACTION_DELETE_MESSAGE)
                intent.putExtra(EXTRA_MESSAGE_ID, currentMessage!!.id)
                item.intent = intent
            }
        }

        open fun fillInView(message: MessageWrapper) {
            senderView.text = message.senderName
            dateView.text = message.createdDateFormatted
            contentView.text = message.markupContent
            currentMessage = message

            val dh = if (message.isShouldDisplayHeader) View.VISIBLE else View.GONE
            senderView.visibility = dh
            dateView.visibility = dh
            imageView.visibility = dh

            val resources = context.resources
            val imageWidth = resources.getDimensionPixelSize(R.dimen.chat_image_width)
            val imageHeight = resources.getDimensionPixelSize(R.dimen.chat_image_height)

            updateIndex++
            val oldUpdateIndex = updateIndex
            imageCache!!.loadImageFromApi("/users/" + message.sender + "/image", imageWidth, imageHeight, StorageType.LONG,
                    object : LoadImageCallback {
                        override fun bitmapLoaded(bitmap: Bitmap) {
                            if (updateIndex == oldUpdateIndex) {
                                imageView.setImageDrawable(BitmapDrawable(resources, bitmap))
                            }
                        }

                        override fun bitmapNotFound() {
                            if (updateIndex == oldUpdateIndex) {
                                imageView.setImageDrawable(ColorDrawable(Color.GREEN))
                            }
                        }
                    })
        }
    }

    inner class MessageViewHolderExtraContent(itemView: View) : MessageViewHolder(itemView) {
        private val imageView: ImageView
        private val htmlContentView: TextView
        private var imageLoadIndex: Long = 0

        init {
            imageView = itemView.findViewById(R.id.message_image) as ImageView
            htmlContentView = itemView.findViewById(R.id.extra_content_html) as TextView
        }

        override fun fillInView(message: MessageWrapper) {
            super.fillInView(message)

            if (message.extraHtml != null) {
                htmlContentView.text = Html.fromHtml(message.extraHtml)
                htmlContentView.visibility = View.VISIBLE
            }
            else {
                htmlContentView.text = ""
                htmlContentView.visibility = View.GONE
            }

            val messageImage = message.image
            val refIndex = ++imageLoadIndex
            if (messageImage != null) {
                imageView.visibility = View.GONE
                val callback = object : LoadImageCallback {
                    override fun bitmapLoaded(bitmap: Bitmap) {
                        if (imageLoadIndex == refIndex) {
                            imageView.setImageDrawable(BitmapDrawable(context.resources, bitmap))
                            imageView.visibility = View.VISIBLE
                        }
                    }

                    override fun bitmapNotFound() {
                        // Do nothing
                    }
                }
                val res = context.resources
                val imageWidth = res.getDimensionPixelSize(R.dimen.message_image_width)
                val imageHeight = res.getDimensionPixelSize(R.dimen.message_image_height)
                imageCache!!.loadImageFromApi(messageImage.file, imageWidth, imageHeight, StorageType.LONG, callback)
            }
            else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }
    }

    private inner class EndOfChannelMarkerViewHolder(view: View) : ViewHolder(view)

    inner class MessageElementItemDecoration : RecyclerView.ItemDecoration() {
        val divider: Drawable
        val dividerVerticalMargin: Int

        init {
            val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
            divider = attrs.getDrawable(0)
            attrs.recycle()

            dividerVerticalMargin = context.resources.getDimension(R.dimen.channel_content_message_block_separator).toInt()
        }

        fun messageFromAdapterPos(parent: RecyclerView, view: View): MessageWrapper? {
            var pos = parent.getChildAdapterPosition(view)
            if (pos > 0 && pos < messages.size) {
                val msg = messages[pos]
                if (msg.isShouldDisplayHeader) {
                    return msg
                }
            }
            return null
        }

        override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
            var top = 0
            if (messageFromAdapterPos(parent!!, view!!) != null) {
                top = divider.intrinsicHeight + dividerVerticalMargin * 2
            }
            outRect!!.set(0, top, 0, 0)
        }

        override fun onDraw(canvas: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
            val left = parent!!.getPaddingLeft();
            val right = parent.width - parent.paddingRight;

            for (i in 0..(parent.childCount - 1)) {
                val view = parent.getChildAt(i)
                val msg = messageFromAdapterPos(parent, view)
                if (msg != null) {
                    //val p = view.layoutParams as RecyclerView.LayoutParams
                    val top = view.top - dividerVerticalMargin - divider.intrinsicHeight
                    val bottom = top + divider.intrinsicHeight
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(canvas)
                }
            }
        }
    }
}

interface LoadMessagesCallback {
    fun loadSuccessful(messages: List<MessageWrapper>)
    fun loadFailed(errorMessage: String)
}
