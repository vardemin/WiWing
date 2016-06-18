package com.vardemin.wiwing;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vardemin.wiwing.models.NewsModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class WiNewsFragment extends Fragment implements View.OnClickListener, NewsListener{
    private final int SELECT_PHOTO = 1;

    private NewsModel currentNew;
    private EditText header;
    private EditText content;

    private WiMenu ctx;
    private List<News> news;
    protected RecyclerView rv;
    protected RecyclerView.LayoutManager layoutManager;
    protected WiNewsAdapter adapter;

    private Realm realm;

    public WiNewsFragment() {
        news = new ArrayList<>();
        realm = Realm.getDefaultInstance();
        List<NewsModel> newsModels = realm.where(NewsModel.class).findAll();
        for (NewsModel nm: newsModels)
        {
            news.add(nm.getNew());
        }
        realm.close();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        ctx = (WiMenu) getActivity();
        rv=(RecyclerView)v.findViewById(R.id.news_recycler_view);

        layoutManager = new LinearLayoutManager(ctx);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);

        currentNew = new NewsModel(ctx.getCurrentUser().getUuid());
        Button postBtn = (Button)v.findViewById(R.id.news_button);
        postBtn.setOnClickListener(this);
        header = (EditText)v.findViewById(R.id.news_header);
        content = (EditText)v.findViewById(R.id.news_text);
        realm = Realm.getDefaultInstance();
        adapter = new WiNewsAdapter(news,realm);
        rv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.news_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_attach_photo:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                break;
            case R.id.menuitem_clean_news:
                RealmResults<NewsModel> models = realm.where(NewsModel.class).findAll();
                realm.beginTransaction();
                models.deleteAllFromRealm();
                realm.commitTransaction();
                adapter.news.clear();
                adapter.notifyDataSetChanged();
            default: break;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        selectedImage = Utility.cropToSquare(selectedImage);
                        selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        if (currentNew!=null) {
                            currentNew.setAddition(stream.toByteArray());
                            currentNew.setType((byte) 0);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.news_button:
                if (!header.getText().toString().equals("") || !content.getText().toString().equals("")) {
                    currentNew.setHeader(header.getText().toString());
                    currentNew.setContent(content.getText().toString());
                    realm.beginTransaction();
                    realm.copyToRealm(currentNew);
                    realm.commitTransaction();
                    WiSync.getInstance().broadcastPacket(new WiWingPacket(WiPackageType.onNews, currentNew.getNew()));
                    onNewsReceive(currentNew.getNew());
                    currentNew = new NewsModel(ctx.getCurrentUser().getUuid());
                    header.setText("");
                    content.setText("");

                }
                else Toast.makeText(ctx,"Fill all fields",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onNewsReceive(News news) {
        this.news.add(news);
        if (adapter != null) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
