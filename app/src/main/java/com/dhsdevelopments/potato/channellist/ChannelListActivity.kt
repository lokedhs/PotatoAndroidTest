package com.dhsdevelopments.potato.channellist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channelmessages.ChannelContentActivity
import com.dhsdevelopments.potato.channelmessages.ChannelContentFragment
import com.dhsdevelopments.potato.channelmessages.HasChannelContentActivity
import com.dhsdevelopments.potato.common.IntentUtil
import com.dhsdevelopments.potato.common.RemoteRequestService
import com.dhsdevelopments.potato.selectchannel.SelectChannelActivity
import com.dhsdevelopments.potato.settings.SettingsActivity
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker
import com.dhsdevelopments.potato.userlist.UserListFragment
import com.dhsdevelopments.potato.wearable.WatchSendService

class ChannelListActivity : AppCompatActivity(), HasChannelContentActivity {
    private var selectedDomainId: String? = null

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    var isTwoPane: Boolean = false
        private set

    /**
     * In two pane mode, this activity needs to hold the channel users tracker for the selected channel.
     */
    private var usersTracker: ChannelUsersTracker? = null
    private lateinit var channelListAdapter: ChannelListAdapter
    private lateinit var receiver: BroadcastReceiver
    private lateinit var toggle: ActionBarDrawerToggle

    private var userListFragment: UserListFragment? = null
    private var channelContentFragment: ChannelContentFragment? = null

    private val navigationView          by lazy { findViewById<NavigationView>(R.id.channel_list_nav_view) }
    private val domainsMenu             by lazy { navigationView.menu.findItem(R.id.nav_domain_menu).subMenu }
    private val swipeRefreshLayout      by lazy { findViewById<SwipeRefreshLayout>(R.id.channel_list_refresh) }
    private val channelListRecyclerView by lazy { findViewById<RecyclerView>(R.id.channel_list) }
    private val drawer                  by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        selectedDomainId = if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_SELECTED_DOMAIN_ID)
        }
        else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.getString(resources.getString(R.string.pref_key_default_domain), null)
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        navigationView.setNavigationItemSelectedListener { item -> handleNavigationItemSelected(item) }

        channelListAdapter = ChannelListAdapter(this)
        channelListRecyclerView.adapter = channelListAdapter

        if (findViewById<View>(R.id.channel_detail_container) != null) {
            isTwoPane = true
        }

        createToggle(toolbar)

        swipeRefreshLayout.setOnRefreshListener { RemoteRequestService.loadChannelList(this) }
    }

    private fun createToggle(toolbar: Toolbar) {
        toggle = object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                if (drawerView.id == R.id.channel_list_nav_view) {
                    super.onDrawerSlide(drawerView, slideOffset)
                }
            }
        }

        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onStart() {
        super.onStart()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBroadcastMessage(intent)
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED)
            addAction(RemoteRequestService.ACTION_CHANNEL_LIST_UPDATE_FAIL)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        updateDomainList()
        RemoteRequestService.loadChannelList(this)

        WatchSendService.sendApiKey(this)
    }

    private fun handleBroadcastMessage(intent: Intent) {
        when (intent.action) {
            RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED -> updateDomainList()
            RemoteRequestService.ACTION_CHANNEL_LIST_UPDATE_FAIL -> showErrorSnackbar(intent.getStringExtra(RemoteRequestService.EXTRA_ERROR_MESSAGE))
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(channelListRecyclerView, "Error loading channels: $message", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SELECTED_DOMAIN_ID, selectedDomainId)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START)
        }
        else {
            super.onBackPressed()
        }
    }

    override fun closeUserListDrawer() {
        drawer.closeDrawer(Gravity.END)
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(Gravity.START)

        when (item.itemId) {
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            else -> checkMenuAndSwitchToDomain(item)
        }

        return true
    }

    private fun checkMenuAndSwitchToDomain(item: MenuItem) {
        val intent = item.intent
        if (intent != null) {
            selectedDomainId = intent.getStringExtra(EXTRA_DOMAIN_ID)
            activateSelectedDomain()
        }
    }

    private fun activateSelectedDomain() {
        if (selectedDomainId == null && domainsMenu.size() > 0) {
            selectedDomainId = domainsMenu.getItem(0).intent.getStringExtra(EXTRA_DOMAIN_ID)
        }
        if (selectedDomainId != null) {
            var domainName: String? = null
            val n = domainsMenu.size()
            for (i in 0 until n) {
                val item = domainsMenu.getItem(i)
                val isSelectedDomain = item.intent.getStringExtra(EXTRA_DOMAIN_ID) == selectedDomainId
                if (isSelectedDomain) {
                    domainName = item.intent.getStringExtra(EXTRA_DOMAIN_NAME)
                }
                item.isChecked = isSelectedDomain
            }
            title = domainName ?: "No domain selected"
            if (domainName == null) {
                // If we get here, the user has been removed from the currently selected domain, so we'll
                // simply clear the selected domain.
                selectedDomainId = null
            }
            channelListAdapter.selectDomain(selectedDomainId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.channel_list_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home ->
                toggle.onOptionsItemSelected(item)
            R.id.menu_option_join_channel -> {
                selectAndJoinChannel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun selectAndJoinChannel() {
        val intent = Intent(this, SelectChannelActivity::class.java).apply {
            putExtra(IntentUtil.EXTRA_DOMAIN_ID, selectedDomainId)
        }
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                val cid = data!!.getStringExtra(IntentUtil.EXTRA_CHANNEL_ID)
                setActiveChannel(cid)
            }
            SelectChannelActivity.RESULT_ERROR_LOADING_CHANNEL -> {
                val msg = data!!.getStringExtra(IntentUtil.EXTRA_ERROR_MESSAGE)
                Snackbar.make(channelListRecyclerView, "Error loading channel: $msg", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            }
            RESULT_CANCELED -> {
                // Do nothing
            }
            else -> throw RuntimeException("Unexpected return code from select channel activity: $resultCode")
        }
    }

    fun setActiveChannel(cid: String) {
        if (isTwoPane) {
            switchToChannelTwoPane(cid)
        }
        else {
            val intent = Intent(this, ChannelContentActivity::class.java).apply {
                putExtra(ChannelContentFragment.ARG_CHANNEL_ID, cid)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
        }
    }

    private fun switchToChannelTwoPane(cid: String) {
        usersTracker = ChannelUsersTracker.findForChannel(this, cid)

        val arguments = Bundle()
        arguments.putString(ChannelContentFragment.ARG_CHANNEL_ID, cid)

        val fragment = ChannelContentFragment()
        fragment.arguments = arguments

        userListFragment = UserListFragment.newInstance(cid)

        fragmentManager
                .beginTransaction()
                .replace(R.id.channel_detail_container, fragment)
                .replace(R.id.user_list_container, userListFragment)
                .commit()

        channelContentFragment = fragment

        invalidateOptionsMenu()
    }

    override fun findUserTracker(): ChannelUsersTracker = usersTracker!!

    override fun closeChannel() {
        if (channelContentFragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(channelContentFragment)
                    .remove(userListFragment)
                    .commit()

            channelContentFragment = null
            userListFragment = null
        }
    }

    override fun openChannel(cid: String) {
        setActiveChannel(cid)
    }

    private fun updateDomainList() {
        domainsMenu.clear()

        val db = PotatoApplication.getInstance(this).cacheDatabase
        db.domainDao().findAll().forEach { d ->
            val domainId = d.id
            val domainName = d.name

            val item = domainsMenu.add(domainName)
            val intent = Intent().apply {
                putExtra(EXTRA_DOMAIN_ID, domainId)
                putExtra(EXTRA_DOMAIN_NAME, domainName)
            }
            item.intent = intent
        }

        activateSelectedDomain()
    }

    companion object {
        const val EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id"
        const val EXTRA_DOMAIN_NAME = "com.dhsdevelopments.potato.domain_name"

        private const val STATE_SELECTED_DOMAIN_ID = "selectedDomain"
    }
}
