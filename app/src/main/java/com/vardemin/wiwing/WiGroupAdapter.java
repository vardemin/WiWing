package com.vardemin.wiwing;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xavie on 23.05.2016.
 */
public class WiGroupAdapter extends RecyclerView.Adapter<WiGroupAdapter.WiGroupViewHolder> {
    public List<User> users;
    public  WiGroupAdapter(List<User> userList) {
        users = userList;
    }
    @Override
    public WiGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false);
        WiGroupViewHolder wvh = new WiGroupViewHolder(v);
        return wvh;
    }

    @Override
    public void onBindViewHolder(WiGroupViewHolder holder, int position) {
        if (users.get(position).getPhoto()!=null)
            holder.photo.setImageBitmap(Utility.getPhoto(users.get(position).getPhoto()));
        holder.name.setText(users.get(position).getName());
        holder.age.setText(String.valueOf(users.get(position).getAge()));
        if (users.get(position).getStatus()!=null)
            holder.status.setText(users.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class WiGroupViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        ImageView photo;
        TextView name;
        TextView age;
        TextView status;

        public WiGroupViewHolder (View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.group_cv);
            photo = (ImageView)itemView.findViewById(R.id.group_photo);
            name = (TextView)itemView.findViewById(R.id.group_name);
            age = (TextView)itemView.findViewById(R.id.group_age);
            status = (TextView)itemView.findViewById(R.id.group_status);
        }

    }
}
