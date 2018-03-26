package com.dhsdevelopments.potato.channelmessages

import android.app.Activity
import android.app.Fragment
import android.app.SearchManager
import android.content.*
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.Spanned
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.clientapi.message.Message
import com.dhsdevelopments.potato.clientapi.notifications.OptionNotification
import com.dhsdevelopments.potato.clientapi.sendmessage.unreadnotification.SendMessageRequest
import com.dhsdevelopments.potato.clientapi.sendmessage.unreadnotification.SendMessageResult
import com.dhsdevelopments.potato.common.*
import com.dhsdevelopments.potato.editor.UidSpan
import com.dhsdevelopments.potato.editor.UserNameSuggestAdapter
import com.dhsdevelopments.potato.editor.UserNameTokeniser
import com.dhsdevelopments.potato.search.SearchActivity
import com.dhsdevelopments.potato.service.ChannelSubscriptionService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.Collator
import java.text.MessageFormat
import java.util.*

class ChannelContentFragment : Fragment() {

    companion object {
        val ARG_CHANNEL_ID = "item_id"

        private val SELECT_IMAGE_RESULT_CODE = 1
    }

    private lateinit var cid: String
    private lateinit var channelInfo: ChannelDescriptor

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
        caseInsensitiveStringComparator = Comparator { o1, o2 -> collator.compare(o1, o2) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arguments.containsKey(ARG_CHANNEL_ID)) {
            throw IllegalArgumentException("channelId not specified in activity")
        }

        cid = arguments.getString(ARG_CHANNEL_ID) ?: throw IllegalStateException("channelId argument not specified")
        channelInfo = DbTools.loadChannelInfoFromDb(activity, cid)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBroadcastMessage(intent)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_TYPING)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_OPTIONS)
        intentFilter.addAction(ChannelSubscriptionService.ACTION_UNKNOWN_SLASHCOMMAND_RESPONSE)
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, intentFilter)

        adapter = ChannelContentAdapter(this, cid)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
        super.onDestroy()
    }

    var scrollDownPanelVisibility = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_channel_content, container, false)
        messageListView = rootView.findViewById(R.id.message_list)

        setHasOptionsMenu(true)

        val layoutManager = LinearLayoutManager(this.activity)
        messageListView.layoutManager = layoutManager

        messageListView.adapter = adapter

        observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // Only scroll if the message was inserted at the bottom, and we're already looking at
                // the bottom element.
                Log.d("rangeInserted. posStart=$positionStart, count=$itemCount, lastVis=$lastVisibleItem, itemCount=$adapter.itemCount")
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
                // Adjust the last visible item to take the end of channel market into account
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

        messageListView.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            // If the view height has been decreased, and we're looking at the bottom of the
            // message list, make sure that the last element is shown. This usually happens
            // because the soft keyboard has been opened.
            val height = bottom - top
            val oldHeight = oldBottom - oldTop
            if (height < oldHeight) {
                // Ideally we want to scroll down only in the case where we were looking at the bottom
                // of the list, but the tricks I've come up with to detect if this is the case has
                // not been very reliable, so for now we'll just keep the bottom element in view.
                messageListView.scrollBy(0, oldHeight - height)
            }
        }

        val messageInput = rootView.findViewById<MultiAutoCompleteTextView>(R.id.message_input_field)
        messageInput.setImeActionLabel("Send", KeyEvent.KEYCODE_ENTER)
        messageInput.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage(messageInput)
                true
            }
            else {
                false
            }
        }

        val userTracker = (activity as HasChannelContentActivity).findUserTracker()
        userNameSuggestAdapter = UserNameSuggestAdapter(activity, userTracker)
        messageInput.setAdapter<UserNameSuggestAdapter>(userNameSuggestAdapter)
        messageInput.setTokenizer(UserNameTokeniser(userTracker))

        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val sendButton = rootView.findViewById<Button>(R.id.send_button)
        sendButton.setOnClickListener {
            sendMessage(messageInput)
            imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
        }

        typingTextView = rootView.findViewById(R.id.typing_text_view)

        swipeRefreshLayout = rootView.findViewById(R.id.channel_content_refresh)
        swipeRefreshLayout.setOnRefreshListener {
            adapter.loadMoreMessages(object : ChannelContentAdapter.LoadMessagesCallback {
                override fun loadSuccessful(messages: List<MessageWrapper>) {
                    swipeRefreshLayout.isRefreshing = false
                    if (messages.isNotEmpty()) {
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
        scrollDownButton = scrollDownPanel.findViewById(R.id.scroll_to_bottom)
        scrollDownButton.setOnClickListener({ scrollToBottom() })

        return rootView
    }

    override fun onDestroyView() {
        userNameSuggestAdapter.shutdown()
        adapter.unregisterAdapterDataObserver(observer)

        super.onDestroyView()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(messageListView)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(activity, ChannelSubscriptionService::class.java)
        intent.action = ChannelSubscriptionService.ACTION_BIND_TO_CHANNEL
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, cid)
        activity.startService(intent)
        adapter.loadMessages(object : ChannelContentAdapter.LoadMessagesCallback {
            override fun loadSuccessful(messages: List<MessageWrapper>) {
                scrollToBottom()
            }

            override fun loadFailed(errorMessage: String) {
                showErrorSnackbar("Error loading messages: " + errorMessage)
            }
        })

        refreshTypingNotifier()

        RemoteRequestService.markNotificationsForChannel(activity, cid)
    }

    override fun onStop() {
        val intent = Intent(activity, ChannelSubscriptionService::class.java)
        intent.action = ChannelSubscriptionService.ACTION_UNBIND_FROM_CHANNEL
        intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, cid)
        Log.d("Requesting unbind for $cid")
        activity.startService(intent)
        Log.d("After request for unbind for $cid")
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.channel_content_toolbar_menu, menu)

        val notifyUnreadOption = menu.findItem(R.id.menu_option_notify_unread)
        val db = PotatoApplication.getInstance(activity).cacheDatabase

        notifyUnreadOption.isChecked = db.channelConfigDao().findByChannelId(cid)?.showUnread ?: false

        // Set up search
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val item = menu.findItem(R.id.menu_option_search_history)
        val searchView = item.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(activity, SearchActivity::class.java)))
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = getString(R.string.searchable_hint)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.navigateUpTo(Intent(activity, ChannelListActivity::class.java))
                val parent = activity
                if (parent is ChannelContentActivity) {
                    parent.overrideAnimExit()
                }
                return true
            }
            R.id.menu_option_show_users -> {
                val drawer = activity.findViewById<DrawerLayout>(R.id.drawer_layout)
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END)
                }
                else {
                    drawer.openDrawer(GravityCompat.END)
                }
                return true
            }
            R.id.menu_option_notify_unread -> {
                val notifyUnread = !item.isChecked
                item.isChecked = notifyUnread
                updateNotifyUnreadSetting(notifyUnread)
                return true
            }
            R.id.menu_option_send_image -> {
                sendImage()
                return true
            }
            R.id.menu_option_leave_channel -> {
                leaveChannel()
                return true
            }
        //            R.id.menu_option_search_history -> {
        ////                intent = Intent(this, SearchActivity::class.java)
        ////                intent.putExtra(SearchActivity.EXTRA_CHANNEL_ID, channelId)
        ////                startActivity(intent)
        //                Log.d("Open search input")
        //                return true
        //            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("Context menu selected: $item")
        when (item.itemId) {
            R.id.message_popup_delete_message -> {
                Log.i("was delete")
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE_RESULT_CODE) {
                val uri = data!!.data
                RemoteRequestService.sendMessageWithImage(activity, cid, uri)
            }
        }
    }

    private fun scrollToBottom() {
        messageListView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun showErrorSnackbar(message: CharSequence) {
        Snackbar.make(messageListView, message, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    private fun handleBroadcastMessage(intent: Intent) {
        Log.d("received broadcast message of type " + intent.action)
        when (intent.action) {
            ChannelSubscriptionService.ACTION_MESSAGE_RECEIVED -> processMessagePostedNotification(intent)
            ChannelSubscriptionService.ACTION_CHANNEL_USERS_UPDATE -> processChannelUsersNotification(intent)
            ChannelSubscriptionService.ACTION_TYPING -> processTypingNotification(intent)
            ChannelSubscriptionService.ACTION_UNKNOWN_SLASHCOMMAND_RESPONSE -> processUnknownSlashcommand(intent)
            ChannelSubscriptionService.ACTION_OPTIONS -> processOptions(intent)
        }
    }

    private fun processMessagePostedNotification(intent: Intent) {
        val msg = intent.getSerializableExtra(ChannelSubscriptionService.EXTRA_MESSAGE) as Message
        if (msg.channel == cid) {
            adapter.newMessage(msg)
        }
    }

    private fun processChannelUsersNotification(intent: Intent) {
        val tracker = findUserTracker()
        tracker.processIncoming(intent)
    }

    private fun findUserTracker() = (activity as HasChannelContentActivity).findUserTracker()

    private fun processTypingNotification(intent: Intent) {
        if (intent.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID) != cid) {
            // Only process messages on this channel
            return
        }

        val uid = intent.getStringExtra(IntentUtil.EXTRA_USER_ID)
        val mode = intent.getStringExtra(ChannelSubscriptionService.EXTRA_TYPING_MODE)
        when (mode) {
            ChannelSubscriptionService.TYPING_MODE_ADD -> typingUsers.put(uid, findUserTracker().getNameForUid(uid))
            ChannelSubscriptionService.TYPING_MODE_REMOVE -> typingUsers.remove(uid)
            else -> {
                Log.w("Unexpected typing mode in broadcast message: " + mode)
            }
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
                for (i in 0..numUsers - 2) {
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

    private fun processOptions(intent: Intent) {
        val notification = intent.getSerializableExtra(ChannelSubscriptionService.EXTRA_OPTION_NOTIFICATION) as OptionNotification

        val ft = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        val fragment = OptionsDialogFragment.makeDialog(notification)
        fragment.show(ft, "dialog")
    }

    private fun processUnknownSlashcommand(intent: Intent) {
        if (intent.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID) == cid) {
            val cmd = intent.getStringExtra(ChannelSubscriptionService.EXTRA_COMMAND_NAME)
            val fmt = MessageFormat(getString(R.string.illegal_command_reply))
            showErrorSnackbar(fmt.format(arrayOf(cmd)))
        }
    }

    private fun sendMessage(messageInput: EditText) {
        val text = messageInput.text

        if (text.isNotEmpty()) {
            if (text[0] == '/') {
                val result = Regex("^([a-zA-Z0-9_-]+)(?: +([^ ].*))?$").find(text.substring(1))
                if (result != null) {
                    val cmd = result.groupValues[1]
                    val args = result.groupValues[2]
                    RemoteRequestService.sendCommand(activity, cid, cmd, args)
                }
                else {
                    displaySnackbarMessage(messageInput, "Illegal characters in command")
                }
            }
            else {
                val app = PotatoApplication.getInstance(activity)
                val api = app.findApiProvider().makePotatoApi()
                val apiKey = app.findApiKey()
                val call = api.sendMessage(apiKey, cid, SendMessageRequest(convertUidRefs(text)))
                call.enqueue(object : Callback<SendMessageResult> {
                    override fun onResponse(call: Call<SendMessageResult>, response: Response<SendMessageResult>) {
                        if (response.isSuccessful) {
                            Log.i("Created message with id: ${response.body()!!.id}")
                        }
                        else {
                            try {
                                Log.e("Send message error from server: " + response.errorBody()!!.string())
                            }
                            catch (e: IOException) {
                                Log.e("Exception when getting error body after sending message", e)
                            }

                            displaySnackbarMessage(messageInput, "The server responded with an error")
                        }
                    }

                    override fun onFailure(call: Call<SendMessageResult>, t: Throwable) {
                        Log.e("Error sending message to channel", t)
                        displaySnackbarMessage(messageInput, "Error sending message: " + t.message)
                    }
                })
            }

            messageInput.setText("")
        }
    }

    private fun sendImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, getString(R.string.chooser_title_select_image)), SELECT_IMAGE_RESULT_CODE)
    }

    private fun updateNotifyUnreadSetting(notifyUnread: Boolean) {
        RemoteRequestService.updateUnreadSubscriptionState(activity, cid, notifyUnread)
    }

    private fun leaveChannel() {
        if (channelInfo.privateUser == null) {
            RemoteRequestService.leaveChannel(activity, cid)
        }
        else {
            RemoteRequestService.updateChannelVisibility(activity, cid, false)
        }
        (activity as HasChannelContentActivity).closeChannel()
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
