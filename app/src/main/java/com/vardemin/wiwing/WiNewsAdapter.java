package com.vardemin.wiwing;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vardemin.wiwing.models.NewsModel;
import com.vardemin.wiwing.models.UserModel;

import java.util.List;

import io.realm.Realm;

/**
 * Created by xavie on 22.05.2016.
 */
public class WiNewsAdapter extends RecyclerView.Adapter<WiNewsAdapter.WiNewsViewHolder> {

    List<News> news;
    Realm realm;

    public WiNewsAdapter(List<News> news, Realm realm){
        this.news = news;
        this.realm = realm;
    }

    @Override
    public WiNewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        WiNewsViewHolder wvh = new WiNewsViewHolder(v);
        return wvh;
    }

    @Override
    public void onBindViewHolder(WiNewsViewHolder holder, int position) {
        if (news.get(position).getAddition()!=null)
            holder.newsPhoto.setImageBitmap(Utility.getPhoto(news.get(position).getAddition()));
        holder.newsHeader.setText(news.get(position).getHeader());
        holder.newsContent.setText(news.get(position).getContent());
        UserModel user = realm.where(UserModel.class).equalTo("uuid",news.get(position).getUserUUID()).findFirst();
        if (user!=null)
        {
            holder.userName.setText(user.getName());
            if(user.getPhoto()!=null)
                holder.userPhoto.setImageBitmap(Utility.getPhoto(user.getPhoto()));
        }
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public static class WiNewsViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        ImageView newsPhoto;
        ImageView userPhoto;
        TextView userName;
        TextView newsHeader;
        TextView newsContent;

        public WiNewsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.news_cv);
            newsPhoto = (ImageView)itemView.findViewById(R.id.news_item_photo);
            userPhoto = (ImageView)itemView.findViewById(R.id.news_user_photo);
            userName = (TextView)itemView.findViewById(R.id.news_user_name);
            newsHeader = (TextView)itemView.findViewById(R.id.news_item_header);
            newsContent = (TextView)itemView.findViewById(R.id.news_item_content);
        }
    }
}
