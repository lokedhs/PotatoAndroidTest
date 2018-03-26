package com.dhsdevelopments.potato.channelmessages

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
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
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.message.MessageElement
import com.dhsdevelopments.potato.clientapi.message.MessageHistoryResult
import com.dhsdevelopments.potato.common.DateHelper
import com.dhsdevelopments.potato.common.Log
import com.dhsdevelopments.potato.common.RemoteRequestService
import com.dhsdevelopments.potato.imagecache.ImageCache
import com.dhsdevelopments.potato.imagecache.LoadImageCallback
import com.dhsdevelopments.potato.imagecache.StorageType
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.MessageFormat
import java.util.*

class ChannelContentAdapter(private val parent: ChannelContentFragment, private val cid: String) :
        RecyclerView.Adapter<ChannelContentAdapter.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_PLAIN_MESSAGE = 0
        private const val VIEW_TYPE_EXTRA_CONTENT = 1
        private const val VIEW_TYPE_END_OF_CHANNEL_MARKER = 2

        private const val NUM_MESSAGES_PER_LOAD = 20
    }

    private val context: Context
    private lateinit var userTracker: ChannelUsersTracker
    private lateinit var imageCache: ImageCache
    private val dateHelper = DateHelper()

    private var messages: MutableList<MessageWrapper> = ArrayList()
    private var isLoading = false

    val elementDecoration: RecyclerView.ItemDecoration by lazy { MessageElementItemDecoration() }

    init {
        context = parent.activity
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        imageCache = ImageCache(context)
        userTracker = (parent.activity as HasChannelContentActivity).findUserTracker()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        imageCache.close()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    //    fun addItemSelectionListener(listener: (String) -> Unit) {
    //        itemSelectionListeners.add(listener)
    //    }

    private fun parseMessageList(messages: List<Message>): List<MessageWrapper> {
        val result = ArrayList<MessageWrapper>(messages.size)
        var i = 0
        for (m in messages) {
            if (!m.deleted) {
                val msg = MessageWrapper(m, dateHelper)
                result.add(msg)
                if (i > 0 && shouldHideHeader(result[i - 1], msg)) {
                    msg.shouldDisplayHeader = false
                }
                i++
            }
        }
        return result
    }

    private fun loadMessageHistory(startMessageId: String?, callback: (List<Message>) -> Unit, errorCallback: (String) -> Unit) {
        if (isLoading) {
            Log.w("Attempt to load messages while loading is in progress")
            return
        }

        val handler = Handler()

        val app = PotatoApplication.getInstance(context)
        val call = app.findApiProvider().makePotatoApi().loadHistoryAsJson(app.findApiKey(), cid, NUM_MESSAGES_PER_LOAD, startMessageId ?: "now")
        isLoading = true
        call.enqueue(object : Callback<MessageHistoryResult> {
            override fun onResponse(call: Call<MessageHistoryResult>, response: Response<MessageHistoryResult>) {
                handler.post {
                    isLoading = false
                    if (response.isSuccessful) {
                        callback(response.body()!!.messages)
                    }
                    else {
                        Log.e("Server error when loading message history. code=${response.code()}, message=${response.message()}")
                        errorCallback("HTTP error: " + response.code())
                    }
                }
            }

            override fun onFailure(call: Call<MessageHistoryResult>, t: Throwable) {
                Log.e("Error loading message history", t)
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

            messages.clear()
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

    internal fun positionForMessage(messageId: String): Int {
        val i = messages.indexOfFirst { it.id == messageId }
        if (i == -1) {
            throw IllegalStateException("Message with id: $messageId not found")
        }
        return i
    }

    override fun getItemCount(): Int = messages.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == messages.size) {
            VIEW_TYPE_END_OF_CHANNEL_MARKER
        }
        else {
            val m = messages[position]
            if (m.extraHtml == null && m.image == null) VIEW_TYPE_PLAIN_MESSAGE else VIEW_TYPE_EXTRA_CONTENT
        }
    }

    /**
     * Called when a new message is received from the server.
     */
    fun newMessage(msg: Message) {
        val w = MessageWrapper(msg, dateHelper)
        val pos = messages.binarySearch(w)

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

    //    private fun openMessageDetails(msg: MessageWrapper) {
    //        val intent = MessageDetailActivity.makeIntent(context, userTracker, msg)
    //        context.startActivity(intent)
    //    }

    private fun shouldHideHeader(reference: MessageWrapper, msg: MessageWrapper): Boolean {
        if (reference.sender != msg.sender) {
            return false
        }

        // Only collapse messages that are sent within 1 minute of each other
        return msg.createdDate.time - reference.createdDate.time < 60 * 1000
    }

    private fun updateDisplayStateForPosition(pos: Int): Boolean {
        if (pos < messages.size) {
            val msg = messages[pos]
            val shouldDisplay = pos == 0 || !shouldHideHeader(messages[pos - 1], msg)
            if (shouldDisplay && !msg.shouldDisplayHeader || !shouldDisplay && msg.shouldDisplayHeader) {
                msg.shouldDisplayHeader = shouldDisplay
                return true
            }
        }
        return false
    }

    private fun deleteMessage(messageId: String) {
        AlertDialog.Builder(context).setMessage(R.string.delete_message_confirmation_text)
                .setPositiveButton(R.string.delete_message_confirm, { dialog, which ->
                    RemoteRequestService.deleteMessage(context, messageId)
                })
                .setNegativeButton(R.string.delete_message_deny, null)
                .show()
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    open inner class MessageViewHolder(itemView: View) : ViewHolder(itemView) {
        // @formatter:off
        private val senderView         = itemView.findViewById<TextView>(R.id.sender)
        private val dateView           = itemView.findViewById<TextView>(R.id.date)
        private val dateDetailView     = itemView.findViewById<TextView>(R.id.date_detail)
        private val contentView        = itemView.findViewById<TextView>(R.id.content)
        private val senderNicknameView = itemView.findViewById<TextView>(R.id.sender_nickname)
        private val dateWrapperLayout  = itemView.findViewById<View>(R.id.date_wrapper_layout)
        private val imageView          = itemView.findViewById<ImageView>(R.id.image_content)
        // @formatter:on
        private var updateIndex: Long = 0
        private var currentMessage: MessageWrapper? = null

        init {
            //itemView.setOnLongClickListener { openMessageDetails(currentMessage!!); true }
            itemView.setOnCreateContextMenuListener { menu, view, menuInfo -> createContextMenu(menu) }
        }

        private fun createContextMenu(menu: ContextMenu) {
            Log.d("Creating context menu for $menu")
            val msg = currentMessage!!
            val app = PotatoApplication.getInstance(context)

            val fmt = MessageFormat(context.getString(R.string.message_popup_title))
            menu.setHeaderTitle(fmt.format(arrayOf(msg.senderName)))

            parent.activity.menuInflater.inflate(R.menu.message_popup, menu)
            val item = menu.findItem(R.id.message_popup_delete_message)
            item.setOnMenuItemClickListener { deleteMessage(msg.id); true }
            if (msg.sender != app.findUserId()) {
                item.isEnabled = false
            }
        }

        open fun fillInView(message: MessageWrapper) {
            senderView.text = message.senderName
            dateView.text = DateHelper.makeDateDiffString(context, message.createdDate.time)
            dateDetailView.text = message.createdDateFormatted
            contentView.text = message.content.makeSpan(MessageElement.SpanGenerationContext(context))
            currentMessage = message

            val userTracker = ChannelUsersTracker.findForActivity(parent.activity)
            senderNicknameView.text = userTracker.getNicknameForUid(message.sender)

            val dh = if (message.shouldDisplayHeader) View.VISIBLE else View.GONE
            senderView.visibility = dh
            dateWrapperLayout.visibility = dh
            senderNicknameView.visibility = dh
            imageView.visibility = dh

            val resources = context.resources
            val imageWidth = resources.getDimensionPixelSize(R.dimen.chat_image_width)
            val imageHeight = resources.getDimensionPixelSize(R.dimen.chat_image_height)

            val oldUpdateIndex = ++updateIndex
            imageCache.loadImageFromApi("/users/" + message.sender + "/image", imageWidth, imageHeight, StorageType.LONG,
                    object : LoadImageCallback {
                        override fun bitmapLoaded(bitmap: Bitmap) {
                            if (updateIndex == oldUpdateIndex) {
                                imageView.setImageBitmap(bitmap)
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
        private val imageView: ImageView = itemView.findViewById(R.id.message_image)
        private val htmlContentView: TextView = itemView.findViewById(R.id.extra_content_html)
        private var imageLoadIndex: Long = 0

        override fun fillInView(message: MessageWrapper) {
            super.fillInView(message)

            if (message.extraHtml != null) {
                htmlContentView.text = Html.fromHtml(message.extraHtml, 0)
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
                            imageView.setImageBitmap(bitmap)
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
                imageCache.loadImageFromApi(messageImage.file, imageWidth, imageHeight, StorageType.LONG, callback)
            }
            else {
                imageView.setImageDrawable(null)
                imageView.visibility = View.GONE
            }
        }
    }

    private inner class EndOfChannelMarkerViewHolder(view: View) : ViewHolder(view)

    inner class MessageElementItemDecoration : RecyclerView.ItemDecoration() {
        private val divider: Drawable
        private val dividerVerticalMargin: Int

        init {
            val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
            divider = attrs.getDrawable(0)
            attrs.recycle()

            dividerVerticalMargin = context.resources.getDimension(R.dimen.channel_content_message_block_separator).toInt()
        }

        private fun messageFromAdapterPos(parent: RecyclerView, view: View): MessageWrapper? {
            val pos = parent.getChildAdapterPosition(view)
            if (pos > 0 && pos < messages.size) {
                val msg = messages[pos]
                if (msg.shouldDisplayHeader) {
                    return msg
                }
            }
            return null
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            var top = 0
            if (messageFromAdapterPos(parent, view) != null) {
                top = divider.intrinsicHeight + dividerVerticalMargin * 2
            }
            outRect.set(0, top, 0, 0)
        }

        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight

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

    interface LoadMessagesCallback {
        fun loadSuccessful(messages: List<MessageWrapper>)
        fun loadFailed(errorMessage: String)
    }
}
