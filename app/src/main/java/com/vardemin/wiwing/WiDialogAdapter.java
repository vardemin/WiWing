package com.vardemin.wiwing;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vardemin.wiwing.models.DialogModel;
import com.vardemin.wiwing.models.UserModel;

import java.util.List;

import io.realm.Realm;

/**
 * Created by xavie on 29.05.2016.
 */
public class WiDialogAdapter extends RecyclerView.Adapter<WiDialogAdapter.DialogViewHolder> {

    private List<Dialog> dialogList;
    private Context ctx;
    Realm realm;
    private WiMessagesFragment messagesFragment;
    public static class DialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView dialogName;
        TextView dialogUsers;
        public DialogClickHolder dialogClickHolder;

        public DialogViewHolder(View itemView, DialogClickHolder dialogClickHolder) {
            super(itemView);
            this.dialogClickHolder = dialogClickHolder;
            cv = (CardView)itemView.findViewById(R.id.dialog_cv);
            dialogName = (TextView)itemView.findViewById(R.id.dialog_name);
            dialogUsers = (TextView)itemView.findViewById(R.id.dialog_users_txt);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            dialogClickHolder.onDialogClick(v, getAdapterPosition());
        }
        public static interface DialogClickHolder {
            public void onDialogClick (View caller, int position);
        }
    }

    public WiDialogAdapter(Context ctx, List<Dialog> dialogList) {
        this.ctx = ctx;
        this.dialogList = dialogList;
        realm = Realm.getDefaultInstance();
    }

    @Override
    public DialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_item, parent, false);
        DialogViewHolder dialogViewHolder = new DialogViewHolder(v, new DialogViewHolder.DialogClickHolder() {
            @Override
            public void onDialogClick(View caller, int position) {
                OpenMessageFragment(dialogList.get(position));
            }
        });
        return dialogViewHolder;
    }

    @Override
    public void onBindViewHolder(DialogViewHolder holder, int position) {
        holder.dialogName.setText(dialogList.get(position).getName());
        String users_txt = "USERS: ";
        DialogModel model = realm.where(DialogModel.class).equalTo("uuid",(dialogList.get(position).getUUID())).findFirst();
        for (UserModel user: model.getUsersModels())
            users_txt+=user.getName() + " ";
        holder.dialogUsers.setText(users_txt);
    }

    @Override
    public int getItemCount() {
        return dialogList.size();
    }

    private void OpenMessageFragment(Dialog dialog){
        messagesFragment = WiMessagesFragment.getInstance(dialog.getUUID());
        switchContent(messagesFragment);
    }
    public void switchContent(WiMessagesFragment fragment) {
        if (ctx == null)
            return;
        if (ctx instanceof WiMenu) {
            WiMenu menu = (WiMenu) ctx;
            menu.switchContent(fragment);
        }

    }
}
