package com.dhsdevelopments.potato.channelmessages

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channellist.ChannelListActivity
import com.dhsdevelopments.potato.db.queryForChannel
import com.dhsdevelopments.potato.service.RemoteRequestService
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker
import com.dhsdevelopments.potato.userlist.HasUserTracker
import com.dhsdevelopments.potato.userlist.UserListFragment

class ChannelContentActivity : AppCompatActivity(), HasUserTracker {

    companion object {
        private val SELECT_IMAGE_RESULT_CODE = 1
    }

    private lateinit var usersTracker: ChannelUsersTracker
    private lateinit var channelId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        channelId = intent.getStringExtra(ChannelContentFragment.ARG_CHANNEL_ID)
        val channelName = intent.getStringExtra(ChannelContentFragment.ARG_CHANNEL_NAME)

        setContentView(R.layout.activity_channel_content)
        val toolbar = findViewById(R.id.detail_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Show the Up button in the action bar.
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        usersTracker = ChannelUsersTracker.findForChannel(this, channelId)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            val userListFragment = UserListFragment.newInstance(channelId)
            supportFragmentManager.beginTransaction().replace(R.id.user_list_container, userListFragment).commit()

            val contentFragmentArgs = Bundle()
            contentFragmentArgs.putString(ChannelContentFragment.ARG_CHANNEL_ID, channelId)
            contentFragmentArgs.putString(ChannelContentFragment.ARG_CHANNEL_NAME, channelName)
            val channelContentFragment = ChannelContentFragment()
            channelContentFragment.arguments = contentFragmentArgs
            supportFragmentManager.beginTransaction().add(R.id.channel_detail_container, channelContentFragment).commit()

            title = channelName
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.channel_content_drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        }
        else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.channel_content_toolbar_menu, menu)

        val notifyUnreadOption = menu.findItem(R.id.menu_option_notify_unread)
        val db = PotatoApplication.getInstance(this).cacheDatabase
        var notifyUnread = false
        queryForChannel(db, channelId).use { cursor ->
            if (cursor.moveToNext()) {
                notifyUnread = cursor.getInt(0) != 0
            }
        }
        notifyUnreadOption.isChecked = notifyUnread

        // Set up search
        Log.d("Setting up searchable info for $componentName")
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val item = menu.findItem(R.id.menu_option_search_history)
        Log.d("got item: $item")
        val searchView = item.actionView as SearchView
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, SearchActivity::class.java)))
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(true)
        searchView.queryHint = getString(R.string.searchable_hint)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, ChannelListActivity::class.java))
                return true
            }
            R.id.menu_option_show_users -> {
                val drawer = findViewById(R.id.channel_content_drawer_layout) as DrawerLayout
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

    private fun sendImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, getString(R.string.chooser_title_select_image)), SELECT_IMAGE_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGE_RESULT_CODE) {
                val uri = data!!.data
                RemoteRequestService.sendMessageWithImage(this, channelId, uri)
            }
        }
    }

    private fun updateNotifyUnreadSetting(notifyUnread: Boolean) {
        RemoteRequestService.updateUnreadSubscriptionState(this, channelId, notifyUnread)
    }

    override fun findUserTracker(): ChannelUsersTracker {
        return usersTracker
    }
}
