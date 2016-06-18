package com.vardemin.wiwing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by xavie on 30.05.2016.
 */
public class WiMessageAdapter extends RecyclerView.Adapter<WiMessageAdapter.MessageViewHolder>  {

    private List<Message> messages;
    private User currentUser;
    private Context ctx;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.ROOT);

    public WiMessageAdapter(List<Message> messages, User currentUser, Context ctx) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.ctx = ctx;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case LEFT:
                layout = R.layout.message_item_incom;
                break;
            case RIGHT:
                layout = R.layout.message_item_upcom;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        if (!currentUser.getName().equals(messages.get(position).getName()))
            holder.setNameText(message.getName());
        holder.setMessageText(message.getMessage());
        holder.setTimeText(message.getDateTime());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (currentUser.getName().equals(messages.get(position).getName()))
            return RIGHT;
        else
            return LEFT;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView messageText;
        private TextView timeText;

        public MessageViewHolder(View itemView) {
            super(itemView);
            nameText = (TextView)itemView.findViewById(R.id.message_name);
            messageText = (TextView)itemView.findViewById(R.id.message_text);
            timeText = (TextView)itemView.findViewById(R.id.time_text);
        }

        public void setNameText(String nameTXT) {
            if (null==nameText) return;
            nameText.setText(nameTXT);
        }
        public void setMessageText(String messageTXT) {
            if (null==messageText) return;
            messageText.setText(messageTXT);
        }
        public void setTimeText(long time){
            if (null==timeText) return;
            timeText.setText(SIMPLE_DATE_FORMAT.format(time));
        }

    }
}
