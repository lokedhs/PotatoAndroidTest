package com.dhsdevelopments.potato.channelmessages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Spanned
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.SendMessageResult
import com.dhsdevelopments.potato.editor.UidSpan
import com.dhsdevelopments.potato.editor.UserNameSuggestAdapter
import com.dhsdevelopments.potato.editor.UserNameTokeniser
import com.dhsdevelopments.potato.service.ChannelSubscriptionService
import com.dhsdevelopments.potato.service.RemoteRequestService
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.io.IOException
import java.text.Collator
import java.util.*

class ChannelContentFragment : Fragment() {

    companion object {
        @JvmField val ARG_CHANNEL_ID = "item_id"
        @JvmField val ARG_CHANNEL_NAME = "channel_name"
    }

    private lateinit var cid: String
    private var name: String? = null

    private lateinit var messageListView: RecyclerView
    private lateinit var receiver: BroadcastReceiver
    private lateinit var adapter: ChannelContentAdapter
    private lateinit var observer: RecyclerView.AdapterDataObserver
    private lateinit var userNameSuggestAdapter: UserNameSuggestAdapter

    private lateinit var scrollDownPanel: View
    private lateinit var scrollDownButton: Button

    private val typingUsers = HashMap<String, String>()
    private val caseInsensitiveStringComparator: Comparator<String>
    private lateinit var typingTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var lastVisibleItem: Int = 0

    init {
        val collator = Collator.getInstance()
        caseInsensitiveStringComparator = Comparator<kotlin.String> { o1, o2 -> collator.compare(o1, o2) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("savedInstanceState in ChannelContentFragment = " + savedInstanceState)
        if (!arguments.containsKey(ARG_CHANNEL_ID)) {
            throw IllegalArgumentException("channelId not specified in activity")
        }

        cid = arguments.getString(ARG_CHANNEL_ID) ?: throw IllegalStateException("channelId argument not specified")
        name = arguments.getString(ARG_CHANNEL_NAME)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBroadcastMessage(intent)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_TYPING)
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter)

        adapter = ChannelContentAdapter(this, cid)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("someKey", "value0")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        super.onDestroy()
    }

    var scrollDownPanelVisibility = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_channel_content, container, false)
        messageListView = rootView.findViewById(R.id.message_list) as RecyclerView

        val layoutManager = LinearLayoutManager(this.activity)
        messageListView.layoutManager = layoutManager

        messageListView.adapter = adapter

        observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // Only scroll if the message was inserted at the bottom, and we're already looking at
                // the bottom element.
                Log.d("rangeInserted. posStart=" + positionStart + ", count=" + itemCount + ", lastVis=" + lastVisibleItem + ", itemCount=" + adapter.itemCount)
                val numItems = adapter.itemCount - 1
                if (lastVisibleItem >= numItems - itemCount - 1 && numItems == positionStart + itemCount) {
                    Log.d("scrolling view to " + (numItems + 1))
                    messageListView.scrollToPosition(numItems)
                }
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                // After a change, we want to ensure that we can still see the bottom element that
                // was visible before the change.
                messageListView.scrollToPosition(if (lastVisibleItem < adapter.itemCount - 1) lastVisibleItem + 1 else lastVisibleItem)
            }
        }
        adapter.registerAdapterDataObserver(observer)

        messageListView.addItemDecoration(adapter.elementDecoration)

        messageListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                val pos = layoutManager.findLastVisibleItemPosition()
                lastVisibleItem = if (pos == adapter.itemCount - 1) pos - 1 else pos

                // Check if the scroll down button should be displayed
                val newVis = pos < adapter.itemCount - 2
                if (newVis && !scrollDownPanelVisibility) {
                    scrollDownPanelVisibility = true
                    scrollDownPanel.visibility = View.VISIBLE
                    scrollDownPanel.alpha = 0f
                    scrollDownPanel
                            .animate()
                            .alpha(1f)
                }
                else if (!newVis && scrollDownPanelVisibility) {
                    scrollDownPanelVisibility = false
                    scrollDownPanel
                            .animate()
                            .alpha(0f)
                            .withEndAction { scrollDownPanel.visibility = View.GONE }
                }
            }
        })

        val messageInput = rootView.findViewById(R.id.message_input_field) as MultiAutoCompleteTextView
        messageInput.setImeActionLabel("Send", KeyEvent.KEYCODE_ENTER)
        messageInput.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage(messageInput)
                true
            }
            else {
                false
            }
        }

        val userTracker = ChannelUsersTracker.findEnclosingUserTracker(this)
        userNameSuggestAdapter = UserNameSuggestAdapter(context, userTracker)
        messageInput.setAdapter<UserNameSuggestAdapter>(userNameSuggestAdapter)
        messageInput.setTokenizer(UserNameTokeniser(userTracker))

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val sendButton = rootView.findViewById(R.id.send_button) as Button
        sendButton.setOnClickListener {
            sendMessage(messageInput)
            imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
        }

        typingTextView = rootView.findViewById(R.id.typing_text_view) as TextView

        swipeRefreshLayout = rootView.findViewById(R.id.channel_content_refresh) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            adapter.loadMoreMessages(object : ChannelContentAdapter.LoadMessagesCallback {
                override fun loadSuccessful(messages: List<MessageWrapper>) {
                    swipeRefreshLayout.isRefreshing = false
                    if (messages.size > 0) {
                        messageListView.scrollToPosition(adapter.positionForMessage(messages[messages.size - 1].id))
                    }
                }

                override fun loadFailed(errorMessage: String) {
                    swipeRefreshLayout.isRefreshing = false
                    showErrorSnackbar("Error loading messages: " + errorMessage)
                }
            })
        }

        scrollDownPanel = rootView.findViewById(R.id.scroll_down_panel)
        scrollDownButton = scrollDownPanel.findViewById(R.id.scroll_to_bottom) as Button
        scrollDownButton.setOnClickListener({ scrollToBottom() })

        return rootView
    }

    private fun scrollToBottom() {
        Log.i("Scrolling to bottom")
        messageListView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun showErrorSnackbar(message: CharSequence) {
        Snackbar.make(messageListView, message, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    override fun onDestroyView() {
        userNameSuggestAdapter.shutdown()
        adapter.unregisterAdapterDataObserver(observer)
        super.onDestroyView()
    }

    private fun handleBroadcastMessage(intent: Intent) {
        Log.i("received broadcast message of type " + intent.action)
        when (intent.action) {
            ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED -> processMessagePostedNotification(intent)
            ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE -> processChannelUsersNotification(intent)
            ChannelSubscriptionService.ACTION_TYPING -> processTypingNotification(intent)
        }
    }

    private fun processMessagePostedNotification(intent: Intent) {
        val msg = intent.getSerializableExtra(ChannelSubscriptionService.EXTRA_MESSAGE) as Message
        if (msg.channel == cid) {
            adapter.newMessage(msg)
        }
    }

    private fun processChannelUsersNotification(intent: Intent) {
        val tracker = ChannelUsersTracker.findEnclosingUserTracker(this)
        tracker.processIncoming(intent)
    }

    private fun processTypingNotification(intent: Intent) {
        if (intent.getStringExtra(ChannelSubscriptionService.EXTRA_CHANNEL_ID) != cid) {
            // Only process messages on this channel
            return
        }

        val uid = intent.getStringExtra(ChannelSubscriptionService.EXTRA_USER_ID)
        val mode = intent.getStringExtra(ChannelSubscriptionService.EXTRA_TYPING_MODE)
        Log.i("uid=$uid, mode=$mode")
        when (mode) {
            ChannelSubscriptionService.TYPING_MODE_ADD -> typingUsers.put(uid, ChannelUsersTracker.findEnclosingUserTracker(this).getNameForUid(uid))
            ChannelSubscriptionService.TYPING_MODE_REMOVE -> typingUsers.remove(uid)
            else -> Log.w("Unexpected typing mode in broadcast message: " + mode)
        }

        refreshTypingNotifier()
    }

    private fun refreshTypingNotifier() {
        if (typingUsers.isEmpty()) {
            typingTextView.visibility = View.INVISIBLE
        }
        else {
            val users = ArrayList(typingUsers.values)
            Collections.sort(users, caseInsensitiveStringComparator)

            val buf = StringBuilder()
            val numUsers = users.size
            if (numUsers == 1) {
                buf.append(users[0])
                buf.append(" is typing")
            }
            else {
                for (i in 0..numUsers - 1 - 1) {
                    buf.append(users[i])
                    if (i < numUsers - 2) {
                        buf.append(", ")
                    }
                }
                buf.append(" and ")
                buf.append(users[numUsers - 1])
                buf.append(" are typing")
            }

            typingTextView.text = buf.toString()
            typingTextView.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(context, ChannelSubscriptionService::class.java)
        intent.action = ChannelSubscriptionService.ACTION_BIND_TO_CHANNEL
        intent.putExtra(ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid)
        context.startService(intent)
        adapter.loadMessages(object : ChannelContentAdapter.LoadMessagesCallback {
            override fun loadSuccessful(messages: List<MessageWrapper>) {
                scrollToBottom()
            }

            override fun loadFailed(errorMessage: String) {
                showErrorSnackbar("Error loading messages: " + errorMessage)
            }
        })

        refreshTypingNotifier()

        RemoteRequestService.markNotificationsForChannel(context, cid)
    }

    override fun onStop() {
        val intent = Intent(context, ChannelSubscriptionService::class.java)
        intent.action = ChannelSubscriptionService.ACTION_UNBIND_FROM_CHANNEL
        intent.putExtra(ChannelSubscriptionService.EXTRA_CHANNEL_ID, cid)
        context.startService(intent)
        super.onStop()
    }

    private fun sendMessage(messageInput: EditText) {
        val text = messageInput.text

        if (text.length > 0) {
            val app = PotatoApplication.getInstance(context)
            val api = app.potatoApi
            val apiKey = app.apiKey
            val call = api.sendMessage(apiKey, cid, SendMessageRequest(convertUidRefs(text)))
            call.enqueue(object : Callback<SendMessageResult> {
                override fun onResponse(response: Response<SendMessageResult>, retrofit: Retrofit) {
                    if (response.isSuccess) {
                        Log.i("Created message with id: " + response.body().id)
                    }
                    else {
                        try {
                            Log.e("Send message error from server: " + response.errorBody().string())
                        }
                        catch (e: IOException) {
                            Log.e("Exception when getting error body after sending message", e)
                        }

                        displaySnackbarMessage(messageInput, "The server responded with an error")
                    }
                }

                override fun onFailure(t: Throwable) {
                    Log.e("Error sending message to channel", t)
                    displaySnackbarMessage(messageInput, "Error sending message: " + t.message)
                }
            })

            messageInput.setText("")
        }
    }

    private fun convertUidRefs(text: CharSequence): String {
        if (text is Spanned) {
            val buf = StringBuilder()
            val length = text.length
            val spans = text.getSpans(0, length, UidSpan::class.java)
            var pos = 0
            for (span in spans) {
                val start = text.getSpanStart(span)
                if (start > pos) {
                    buf.append(text.subSequence(pos, start))
                }
                buf.append("\uDB80\uDC01user:")
                buf.append(span.uid)
                buf.append(":")
                buf.append(span.name)
                buf.append("\uDB80\uDC01")
                pos = text.getSpanEnd(span)
            }
            if (pos < length) {
                buf.append(text.subSequence(pos, length))
            }
            return buf.toString()
        }
        else {
            return text.toString()
        }
    }

    private fun displaySnackbarMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }
}
