package com.vardemin.wiwing;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.vardemin.wiwing.models.DialogModel;
import com.vardemin.wiwing.models.MessageModel;
import com.vardemin.wiwing.models.UserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class WiMessagesFragment extends Fragment implements View.OnClickListener, MessagesListener {

    private List<Message> messages;
    private Context ctx;
    //private Dialog dialog;

    protected RecyclerView rv;
    protected RecyclerView.LayoutManager layoutManager;
    private WiMessageAdapter adapter;

    private EditText msg_content;
    private MessageModel messageModel;

    private String dialUUID;

    private Realm realm;

    public WiMessagesFragment() {}
    public String getDialogUUID() {return dialUUID;}

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
    }

    public static WiMessagesFragment getInstance(String dialogUUID)
    {
        WiMessagesFragment fragment = new WiMessagesFragment();
        fragment.dialUUID = dialogUUID;
        return fragment;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        ctx = getActivity();
        msg_content = (EditText)v.findViewById(R.id.message_edit_text);
        ImageView send = (ImageView)v.findViewById(R.id.chat_send_btn);
        send.setOnClickListener(this);

        messages = new ArrayList<>();
        if (realm==null)
            realm = Realm.getDefaultInstance();
        DialogModel dialogModel = realm.where(DialogModel.class).equalTo("uuid",dialUUID).findFirst();
        for (MessageModel model: dialogModel.getMessagesModels())
            messages.add(model.getMessage());
        rv=(RecyclerView)v.findViewById(R.id.messages_recycler_view);

        layoutManager = new LinearLayoutManager(ctx);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        adapter = new WiMessageAdapter(messages,((WiMenu)ctx).getCurrentUser(), ctx);
        rv.setAdapter(adapter);
        messageModel = new MessageModel();
        return v;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.chat_send_btn:
                if(msg_content.getText().toString().equals(""))
                    Toast.makeText(ctx,"Fill text content",Toast.LENGTH_SHORT).show();
                else {
                    DialogModel dialogModel = realm.where(DialogModel.class).equalTo("uuid",dialUUID).findFirst();
                    List<User> _users = new ArrayList<>();
                    _users.addAll(dialogModel.getUsers());
                    messageModel.setDialogUUID(dialUUID);
                    messageModel.setName(((WiMenu) getActivity()).getCurrentUser().getName());
                    messageModel.setMessage(msg_content.getText().toString());
                    messageModel.setDateTime(new Date().getTime());
                    WiSync.getInstance().sendToList(_users, new WiWingPacket(WiPackageType.onMessage, messageModel.getMessage()));
                    realm.beginTransaction();
                    realm.copyToRealm(messageModel);
                    realm.commitTransaction();
                    onMessageReceive(messageModel.getMessage());
                    messageModel = new MessageModel();
                    msg_content.setText("");
                }
                break;
        }
    }

    @Override
    public void onMessageReceive(Message msg) {
        messages.add(msg);
        if (adapter!=null)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
