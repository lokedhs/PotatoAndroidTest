package com.dhsdevelopments.potato.channelmessages

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import com.dhsdevelopments.potato.IntentUtil
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.loadChannelInfoFromDb
import com.dhsdevelopments.potato.nlazy
import com.dhsdevelopments.potato.userlist.ChannelUsersTracker
import com.dhsdevelopments.potato.userlist.UserListFragment

class ChannelContentActivity : AppCompatActivity(), HasChannelContentActivity {

    private lateinit var usersTracker: ChannelUsersTracker
    private lateinit var channelId: String

    private val drawer by nlazy { findViewById(R.id.drawer_layout) as DrawerLayout }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        channelId = intent.getStringExtra(ChannelContentFragment.ARG_CHANNEL_ID)

        setContentView(R.layout.activity_channel_content)
        val toolbar = findViewById(R.id.detail_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Show the Up button in the action bar.
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

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

            val contentFragmentArgs = Bundle()
            contentFragmentArgs.putString(ChannelContentFragment.ARG_CHANNEL_ID, channelId)
            val channelContentFragment = ChannelContentFragment()
            channelContentFragment.arguments = contentFragmentArgs

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.user_list_container, userListFragment)
                    .add(R.id.channel_detail_container, channelContentFragment)
                    .commit()

            title = loadChannelInfoFromDb(this, channelId).name
        }
    }

    fun overrideAnimExit() {
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        }
        else {
            super.onBackPressed()
            overrideAnimExit()
        }
    }

    override fun closeUserListDrawer() {
        drawer.closeDrawer(Gravity.END)
    }

    /**
     * Override the startActivity method in order to add the channel id
     * in the the intent sent to SearchActivity
     */
    override fun startActivity(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            intent.putExtra(IntentUtil.EXTRA_CHANNEL_ID, channelId)
        }
        super.startActivity(intent)
    }

    override fun findUserTracker(): ChannelUsersTracker {
        return usersTracker
    }

    override fun closeChannel() {
        finish()
    }

    override fun openChannel(cid: String) {
        val intent = Intent(this, ChannelContentActivity::class.java)
        intent.putExtra(ChannelContentFragment.ARG_CHANNEL_ID, cid)
        startActivity(intent)
    }
}

interface HasChannelContentActivity {
    fun findUserTracker(): ChannelUsersTracker
    fun closeChannel(): Unit
    fun closeUserListDrawer(): Unit
    fun openChannel(cid: String)
}
