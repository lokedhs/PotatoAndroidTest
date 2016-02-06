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
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.StorageHelper
import com.dhsdevelopments.potato.channelmessages.HasUserTracker
import com.dhsdevelopments.potato.selectchannel.SelectChannelActivity
import com.dhsdevelopments.potato.service.RemoteRequestService
import com.dhsdevelopments.potato.settings.SettingsActivity
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker

class ChannelListActivity : AppCompatActivity(), HasUserTracker {

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
    private var channelListAdapter: ChannelListAdapter? = null
    private var domainsMenu: SubMenu? = null
    private var receiver: BroadcastReceiver? = null

    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById(R.id.channel_list_refresh) as SwipeRefreshLayout
    }

    private val channelListRecyclerView: View by lazy {
        findViewById(R.id.channel_list)!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        if (savedInstanceState != null) {
            selectedDomainId = savedInstanceState.getString(STATE_SELECTED_DOMAIN_ID)
        }
        else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            selectedDomainId = prefs.getString(resources.getString(R.string.pref_key_default_domain), null)
        }

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = title

        val navigationView = findViewById(R.id.channel_list_nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener { item -> handleNavigationItemSelected(item) }
        domainsMenu = navigationView.menu.findItem(R.id.nav_domain_menu).subMenu

        val recyclerView = channelListRecyclerView
        setupRecyclerView(recyclerView as RecyclerView)

        if (findViewById(R.id.channel_detail_container) != null) {
            isTwoPane = true
        }

        val drawer = findViewById(R.id.channel_list_drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        swipeRefreshLayout.setOnRefreshListener({ RemoteRequestService.loadChannelList(this) })
    }

    override fun onStart() {
        super.onStart()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                handleBroadcastMessage(intent)
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED)
        intentFilter.addAction(RemoteRequestService.ACTION_CHANNEL_LIST_UPDATE_FAIL)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter)

        updateDomainList()
        RemoteRequestService.loadChannelList(this)
    }

    private fun handleBroadcastMessage(intent: Intent) {
        Log.i("got broadcast message: " + intent)
        when(intent.action) {
            RemoteRequestService.ACTION_CHANNEL_LIST_UPDATED -> updateDomainList()
            RemoteRequestService.ACTION_CHANNEL_LIST_UPDATE_FAIL -> showErrorSnackbar(intent.getStringExtra(RemoteRequestService.EXTRA_ERROR_MESSAGE))
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make( channelListRecyclerView, "Error loading channels: " + message, Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
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
        val drawer = findViewById(R.id.channel_list_drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }
        else {
            super.onBackPressed()
        }
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = findViewById(R.id.channel_list_drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)

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
        if (selectedDomainId != null) {
            var domainName: String? = null
            val n = domainsMenu!!.size()
            for (i in 0..n - 1) {
                val item = domainsMenu!!.getItem(i)
                val isSelectedDomain = item.intent.getStringExtra(EXTRA_DOMAIN_ID) == selectedDomainId
                if (isSelectedDomain) {
                    domainName = item.intent.getStringExtra(EXTRA_DOMAIN_NAME)
                }
                item.isChecked = isSelectedDomain
            }
            title = if (domainName == null) "No domain selected" else domainName
            if (domainName == null) {
                // If we get here, the user has been removed from the currently selected domain, so we'll
                // simply clear the selected domain.
                selectedDomainId = null
            }
            channelListAdapter!!.selectDomain(selectedDomainId)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        channelListAdapter = ChannelListAdapter(this)
        recyclerView.adapter = channelListAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.channel_list_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_option_join_channel -> {
                selectAndJoinChannel()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun selectAndJoinChannel() {
        val intent = Intent(this, SelectChannelActivity::class.java)
        intent.putExtra(SelectChannelActivity.EXTRA_DOMAIN_ID, selectedDomainId)
        startActivityForResult(intent, 0, null)
    }

    fun setActiveChannel(channelId: String) {
        usersTracker = ChannelUsersTracker.findForChannel(this, channelId)
    }

    override fun getUsersTracker(): ChannelUsersTracker {
        return usersTracker!!
    }

    fun updateDomainList() {
        val db = PotatoApplication.getInstance(this).cacheDatabase

        domainsMenu!!.clear()

        db.query(StorageHelper.DOMAINS_TABLE,
                arrayOf(StorageHelper.DOMAINS_ID, StorageHelper.DOMAINS_NAME),
                null, null, null, null, StorageHelper.DOMAINS_NAME).use { result ->
            while (result.moveToNext()) {
                val domainId = result.getString(0)
                val domainName = result.getString(1)

                val item = domainsMenu!!.add(domainName)
                val intent = Intent()
                intent.putExtra(EXTRA_DOMAIN_ID, domainId)
                intent.putExtra(EXTRA_DOMAIN_NAME, domainName)
                item.intent = intent
            }
        }

        activateSelectedDomain()
    }

    companion object {
        val EXTRA_DOMAIN_ID = "com.dhsdevelopments.potato.domain_id"
        val EXTRA_DOMAIN_NAME = "com.dhsdevelopments.potato.domain_name"

        private val STATE_SELECTED_DOMAIN_ID = "selectedDomain"
    }
}
