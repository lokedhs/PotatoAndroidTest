package com.dhsdevelopments.potato.userlist

import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.channelmessages.HasChannelContentActivity

class UserListFragment : Fragment() {

    private var cid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            cid = arguments.getString(ARG_CHANNEL_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_user_list, container, false)

        val recyclerView = view.findViewById(R.id.user_list_recycler_view) as RecyclerView
        recyclerView.adapter = UserListAdapter((activity as HasChannelContentActivity).findUserTracker())

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        private val ARG_CHANNEL_ID = "channelId"

        fun newInstance(cid: String): UserListFragment {
            val fragment = UserListFragment()
            val args = Bundle()
            args.putString(ARG_CHANNEL_ID, cid)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
