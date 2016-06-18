package com.vardemin.wiwing.fragments;


import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.vardemin.wiwing.R;
import com.vardemin.wiwing.User;
import com.vardemin.wiwing.Utility;
import com.vardemin.wiwing.models.UserModel;

import io.realm.Realm;
import io.realm.exceptions.RealmMigrationNeededException;


public class UserFragment extends Fragment implements View.OnClickListener{

    private User currentUser;

    private EditText name;
    private EditText age;
    private EditText status;
    private ImageView photo;

    private Realm realm;
    public UserFragment() {
        // Required empty public constructor
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
            currentUser = getArguments().getParcelable("user");
        }

    }
    public static UserFragment newInstance(User user) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user, container, false);
        name = (EditText) v.findViewById(R.id.user_name);
        age = (EditText) v.findViewById(R.id.user_age);
        status = (EditText) v.findViewById(R.id.user_status);
        Button applyBtn = (Button)v.findViewById(R.id.user_apply_btn);
        applyBtn.setOnClickListener(this);
        photo = (ImageView) v.findViewById(R.id.user_photo);
        if(currentUser!=null)
        {
            name.setText(currentUser.getName());
            age.setText(String.valueOf(currentUser.getAge()));
            if(currentUser.getStatus()!=null)
            status.setText(currentUser.getStatus());
            if (currentUser.getPhoto()!=null)
            photo.setImageBitmap(Utility.getPhoto(currentUser.getPhoto()));
        }
        return v;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.user_apply_btn:
                UserModel usr = realm.where(UserModel.class).equalTo("name", currentUser.getName()).findFirst();
                if (usr!=null) {
                    realm.beginTransaction();
                    usr.setName(name.getText().toString());
                    usr.setAge(Integer.valueOf(age.getText().toString()));
                    if (photo.getDrawable()!=null)
                        usr.setPhoto(Utility.getBytes(((BitmapDrawable)photo.getDrawable()).getBitmap()));
                    if (status.getText()!=null)
                        usr.setStatus(status.getText().toString());
                    realm.commitTransaction();
                }
        }
    }
}
