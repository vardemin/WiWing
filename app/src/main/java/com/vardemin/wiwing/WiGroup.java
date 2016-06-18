package com.vardemin.wiwing;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WiGroup extends Fragment implements UsersListener {

    private WiMenu ctx;
    protected RecyclerView rv;
    protected RecyclerView.LayoutManager layoutManager;
    protected WiGroupAdapter adapter;
    private List<User> users;

    public WiGroup() {
        users = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_group, container, false);
        ctx = (WiMenu) getActivity();
        rv=(RecyclerView)v.findViewById(R.id.group_recycler_view);

        layoutManager = new LinearLayoutManager(ctx);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new WiGroupAdapter(users);
        rv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onUserReceive(User user) {
        this.users.add(user);
        if (adapter!=null){
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }
    public void Clear() {
        users.clear();
        if (adapter!=null){
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }
    @Override
    public void onUserExit(User user) {
        if (users.contains(user)) {
            this.users.remove(user);
            if (adapter!=null){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        }
    }


    /*@Override
    public void onUsersReceive(List<User> users) {
        adapter.users.clear();
        adapter.users.addAll(users);
        adapter.notifyDataSetChanged();
    }*/
}
