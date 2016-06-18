package com.vardemin.wiwing;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vardemin.wiwing.fragments.UserFragment;
import com.vardemin.wiwing.models.DialogModel;
import com.vardemin.wiwing.models.MessageModel;
import com.vardemin.wiwing.models.NewsModel;
import com.vardemin.wiwing.models.UserModel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class WiMenu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MessagesListener,
        NewsListener, UsersListener, DialogListener {

    private User currentUser;
    private static final String TAG = "WiMenu ACTIVITY";
    private android.support.v4.app.FragmentTransaction ftrans;

    private Socket socket;

    private List<WiClient> clients ;

    /****************Fragments***************/
    private UserFragment userFrag;
    private DevListFragment devListFrag;
    private WiNewsFragment newsFragment;
    private WiGroup groupFragment;
    private WiDialogFragment dialogFragment;

    private List<WiMessagesFragment> messagesFragments;

    private Realm realm;
    private List<MenuActivityListener> menuListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        menuListeners = new ArrayList<>();
        clients = new ArrayList<>();
        messagesFragments = new ArrayList<>();

        realm = Realm.getDefaultInstance();
        Intent i = getIntent();
        currentUser = i.getParcelableExtra("user");
        Log.d(TAG, "User got: NAME - " + currentUser.getName());
        currentUser.setRANG(-1);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);

        ((TextView) headerLayout.findViewById(R.id.menu_user)).setText(currentUser.getName());

        if (currentUser.getStatus()!=null)
            ((TextView) headerLayout.findViewById(R.id.menu_user_desc)).setText(currentUser.getStatus());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        userFrag = UserFragment.newInstance(currentUser);
        devListFrag = new DevListFragment();
        newsFragment = new WiNewsFragment();
        newsFragment.setHasOptionsMenu(true);
        groupFragment = new WiGroup();
        dialogFragment = WiDialogFragment.newInstance();
        dialogFragment.setHasOptionsMenu(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id != R.id.nav_exit)
        ftrans = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_user) {
            ftrans.replace(R.id.frag_container, userFrag, "user");
        } else if (id == R.id.nav_group) {
            ftrans.replace(R.id.frag_container, groupFragment, "group");
        } else if (id == R.id.nav_messages) {
            ftrans.replace(R.id.frag_container, dialogFragment, "dialog");
        } else if (id == R.id.nav_news) {
            ftrans.replace(R.id.frag_container, newsFragment, "news");
        } else if (id == R.id.nav_wifi) {
            ftrans.replace(R.id.frag_container, devListFrag, "devices");

        } else if (id == R.id.nav_options) {

        } else if (id == R.id.nav_exit) {
            finish();
        }
        if (id != R.id.nav_exit) {
            ftrans.addToBackStack(null);
            ftrans.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void addMenuActivityListener(MenuActivityListener listener) {
        menuListeners.add(listener);
    }

    private void fireOnMenuActivityStop() {
        for (MenuActivityListener mListener: menuListeners) {
            mListener.onMenuActivityStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WiManager.newInstance(this).disconnect();
        fireOnMenuActivityStop();
        realm.close();
    }
    @Override
    public synchronized void onMessageReceive(Message msg) {
        final Message message = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageModel msgModel = new MessageModel(message.getDialogUUID(), message.getName(), message.getMessage(), message.getDateTime());
                DialogModel dialog = realm.where(DialogModel.class).equalTo("uuid", message.getDialogUUID()).findFirst();

                if (dialog==null) {
                    realm.beginTransaction();
                    DialogModel _dialog = new DialogModel(message.getDialogUUID());
                    realm.copyToRealm(msgModel);
                    _dialog.addMessage(msgModel);
                    realm.copyToRealm(_dialog);
                    realm.commitTransaction();
                }
                else {
                    realm.beginTransaction();
                    realm.copyToRealm(msgModel);
                    dialog.addMessage(msgModel);
                    realm.commitTransaction();
                }
            }
        });


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat)
                        .setContentTitle(msg.getName())
                        .setContentText(msg.getMessage());

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(), // add this
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        for (WiMessagesFragment fragment: messagesFragments){
            if (msg.getDialogUUID().equals(fragment.getDialogUUID())){
                fragment.onMessageReceive(msg);
                break;
            }
        }

    }
    public User getCurrentUser(){
        return currentUser;
    }

    @Override
    public synchronized void onNewsReceive(News news) {
        final News _news = news;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NewsModel nwsModel = new NewsModel(_news.getUserUUID(), _news.getHeader(), _news.getContent(), _news.getAddition(),
                        _news.getType());
                realm.beginTransaction();
                realm.copyToRealm(nwsModel);
                realm.commitTransaction();
            }
        });

        newsFragment.onNewsReceive(news);
    }

    @Override
    public synchronized void onUserReceive(User user) {
        final User _user = user;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UserModel model = realm.where(UserModel.class).equalTo("uuid",_user.getUuid()).findFirst();
                if (model==null)
                {
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(UserModel.getModel(_user));
                    realm.commitTransaction();
                }
            }
        });

        groupFragment.onUserReceive(user);
    }

    @Override
    public void onUserExit(User user) {
        groupFragment.onUserExit(user);
    }

    @Override
    public synchronized void onDialogCreate(Dialog dialog) {
        final Dialog _dialog = dialog;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogModel model = realm.where(DialogModel.class).equalTo("uuid",_dialog.getUUID()).findFirst();
                if (model==null) {
                    DialogModel dialogModel = new DialogModel(_dialog.getUUID());
                    List<UserModel> userModels = new ArrayList<>();
                    for (String uuid: _dialog.getUsersUUIDList()){
                        UserModel userModel = realm.where(UserModel.class).equalTo("uuid",uuid).findFirst();
                        if (userModel!=null) {
                            userModels.add(userModel);
                        }
                    }
                    dialogModel.setName(_dialog.getName());
                    dialogModel.addUsers(userModels);
                    realm.beginTransaction();
                    realm.copyToRealm(dialogModel);
                    realm.where(UserModel.class).equalTo("uuid",currentUser.getUuid()).findFirst().addDialog(dialogModel);
                    realm.commitTransaction();
                }
            }
        });

        dialogFragment.onDialogCreate(dialog);
    }

    public synchronized void addClient(WiClient client) {
        clients.add(client);
    }
    public synchronized void removeClient(WiClient client){
        if(clients.contains(client))
            clients.remove(client);
    }
    public List<WiClient> getClients() {
        return clients;
    }

    public void switchContent(WiMessagesFragment frag) {
        boolean exist = false;
        for (WiMessagesFragment fragment: messagesFragments) {
            if (frag.getDialogUUID().equals(fragment.getDialogUUID())) {
                frag = fragment;
                exist = true;
            }
        }
        ftrans = getSupportFragmentManager().beginTransaction();
            ftrans.replace(R.id.frag_container, frag);
            ftrans.addToBackStack(null);
            ftrans.commit();
        if (!exist)
            messagesFragments.add(frag);
    }
    public void NotifyLostConnection() {
        groupFragment.Clear();
    }
}

