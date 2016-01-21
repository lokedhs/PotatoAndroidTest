package com.dhsdevelopments.potato.userlist;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.dhsdevelopments.potato.R;

public class UserListFragment extends Fragment
{
    private static final String ARG_CHANNEL_ID = "channelId";

    private String cid;

    public static UserListFragment newInstance( String cid ) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putString( ARG_CHANNEL_ID, cid );
        fragment.setArguments( args );
        return fragment;
    }

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        if( getArguments() != null ) {
            cid = getArguments().getString( ARG_CHANNEL_ID );
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View view = inflater.inflate( R.layout.fragment_user_list, container, false );

        RecyclerView recyclerView = (RecyclerView)view.findViewById( R.id.user_list_recycler_view );
        UserListAdapter userListAdapter = new UserListAdapter( getContext(), cid );
        recyclerView.setAdapter( userListAdapter );
        userListAdapter.loadUsers();

        return view;
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach( context );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
