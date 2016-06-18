package com.vardemin.wiwing;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.vardemin.wiwing.models.DialogModel;
import com.vardemin.wiwing.models.UserModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WiDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WiDialogFragment extends Fragment implements DialogListener, View.OnClickListener {

    private List<Dialog> dialogs;
    private WiMenu ctx;

    protected RecyclerView rv;
    protected RecyclerView.LayoutManager layoutManager;
    private WiDialogAdapter adapter;

    private DialogModel newDialog;

    private EditText dialog_name;
    private TextView added_users_txt;

    private Realm realm;

    public static WiDialogFragment newInstance() {
        WiDialogFragment fragment = new WiDialogFragment();

        return fragment;
    }

    public WiDialogFragment() {
        dialogs = new ArrayList<>();
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.dialogs_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_clear_dialogs:
                RealmResults<DialogModel> models = realm.where(DialogModel.class).findAll();
                realm.beginTransaction();
                models.deleteAllFromRealm();
                realm.commitTransaction();
                dialogs.clear();
                adapter.notifyDataSetChanged();
            default: break;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);
        ctx = (WiMenu)getActivity();

        rv=(RecyclerView)v.findViewById(R.id.dialog_recycler_view);
        layoutManager = new LinearLayoutManager(ctx);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        dialogs = new ArrayList<>();
        newDialog = new DialogModel();
        ImageView addUser = (ImageView)v.findViewById(R.id.dialog_add_user);
        addUser.setOnClickListener(this);
        ImageView addDialog = (ImageView)v.findViewById(R.id.dialog_add);
        addDialog.setOnClickListener(this);
        dialog_name = (EditText)v.findViewById(R.id.dialog_name);
        added_users_txt = (TextView)v.findViewById(R.id.dialog_added_users);
        if (realm==null)
            realm = Realm.getDefaultInstance();
        for (DialogModel model: realm.where(UserModel.class).equalTo("uuid",ctx.getCurrentUser().getUuid()).findFirst().getDialogs())
            dialogs.add(model.getDialog());
        adapter = new WiDialogAdapter(getActivity(),dialogs);
        rv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.dialog_add_user:
                final List<User> userList = WiSync.getInstance().getUsersList();
                AlertDialog dialog;
                final CharSequence[] items = WiSync.getInstance().getUsersNamesList();
                // arraylist to keep the selected items
                final List<Integer> seletedItems=new ArrayList<>();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select From Online Users");
                builder.setMultiChoiceItems(items, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected,
                                                boolean isChecked) {
                                if (isChecked) {
                                    seletedItems.add(indexSelected);
                                } else if (seletedItems.contains(indexSelected)) {
                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                }
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String _usrs = "Added: ";
                                for (int pos : seletedItems) {
                                    UserModel model = realm.where(UserModel.class).equalTo("uuid", userList.get(pos).getUuid()).findFirst();
                                    if (model != null) {
                                        newDialog.addUser(model);
                                        _usrs += model.getName()+ " ";
                                    }
                                }
                                UserModel current_model = realm.where(UserModel.class).equalTo("uuid", ctx.getCurrentUser().getUuid()).findFirst();
                                newDialog.addUser(current_model);
                                _usrs += current_model.getName() + " ";
                                added_users_txt.setText(_usrs);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                //  Your code when user clicked on Cancel
                            }
                        });
                dialog = builder.create();//AlertDialog dialog; create like this outside onClick
                dialog.show();
                break;

            case R.id.dialog_add:
                if (dialog_name.getText().toString().equals("") || newDialog.getUsers().size()<1)
                    Toast.makeText(getActivity(),"FILL ALL FIELDS OR ADD USER",Toast.LENGTH_SHORT).show();
                else {
                    newDialog.setName(dialog_name.getText().toString());
                    WiSync.getInstance().sendToList(newDialog.getUsers(), new WiWingPacket(WiPackageType.onDialog, newDialog.getDialog()));
                    realm.beginTransaction();
                    realm.copyToRealm(newDialog);
                    realm.where(UserModel.class).equalTo("uuid", ctx.getCurrentUser().getUuid()).findFirst().addDialog(newDialog);
                    realm.commitTransaction();
                    onDialogCreate(newDialog.getDialog());
                    newDialog = new DialogModel();
                    added_users_txt.setText("");
                    dialog_name.setText("");
                }
                break;
        }
    }

    @Override
    public void onDialogCreate(Dialog dialog) {
        dialogs.add(dialog);
        if (adapter!=null)
        {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }



}
