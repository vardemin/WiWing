package com.vardemin.wiwing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.app.Activity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.vardemin.wiwing.models.UserModel;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class WiLogin extends AppCompatActivity implements OnClickListener{

    private final int SELECT_PHOTO = 1;

    private Realm realm;
    private Intent menu;

    private ImageView reg_photo;
    private TextView reg_name;
    private TextView login_name;
    private EditText login_password;
    private EditText reg_age;
    private TextView reg_status;
    private EditText reg_password;

    private UserModel usermod;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        reg_photo = (ImageView) findViewById(R.id.login_photo);
        reg_photo.setOnClickListener(this);
        reg_name = (TextView) findViewById(R.id.login_name);
        reg_password = (EditText) findViewById(R.id.login_password);
        login_name = (TextView) findViewById(R.id.login_frag_name);
        login_password = (EditText) findViewById(R.id.login_frag_password);
        reg_age = (EditText) findViewById(R.id.login_age);
        reg_status = (TextView) findViewById(R.id.login_status);

        Button login_btn = (Button) findViewById(R.id.login_button);
        login_btn.setOnClickListener(this);


        Button reg_btn = (Button) findViewById(R.id.register_button);
        reg_btn.setOnClickListener(this);

        realm = Realm.getDefaultInstance();

        menu = new Intent(this, WiMenu.class);
        usermod = new UserModel();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.register_button:
                if (reg_name.getText().toString().equals("") || reg_age.getText().toString().equals("")
                        || reg_password.getText().toString().equals("")) {
                    Toast.makeText(this, "ENTER NAME AND AGE AND PASSWORD",Toast.LENGTH_SHORT).show();break;
                }
                usermod.setName(reg_name.getText().toString());
                usermod.setUuid();
                usermod.setAge(Integer.valueOf(reg_age.getText().toString()));
                usermod.setPassword(reg_password.getText().toString());
                usermod.setIsLocal(true);
                usermod.setStatus(reg_status.getText().toString());
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(usermod);
                realm.commitTransaction();
                menu.putExtra("user", usermod.getUser());
                startActivity(menu);
                finish();
                break;
            case R.id.login_photo:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);break;
            case R.id.login_button:
                if (login_name.getText().equals("") || (login_password.getText().toString().equals(""))) {
                    Toast.makeText(this, "ENTER NAME OR PASSWORD!",Toast.LENGTH_SHORT).show();break;
                }
                UserModel me = realm.where(UserModel.class).equalTo("name", login_name.getText().toString())
                        .equalTo("isLocal",true).findFirst();

                if (me == null) {
                    Toast.makeText(this, "NO SUCH USER",Toast.LENGTH_SHORT).show();break;
                }
                else {
                    if (me.ComparePassword(login_password.getText().toString())) {
                        menu.putExtra("user", me.getUser());
                        startActivity(menu);
                        finish();
                        break;
                    }
                    else {
                        Toast.makeText(this,"INCORRECT PASSWORD",Toast.LENGTH_SHORT).show();break;
                    }
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        String path = imageUri.getPath();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        selectedImage = Utility.cropToSquare(selectedImage);
                        reg_photo.setImageBitmap(selectedImage);
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        usermod.setPhoto(stream.toByteArray());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
    }
}

