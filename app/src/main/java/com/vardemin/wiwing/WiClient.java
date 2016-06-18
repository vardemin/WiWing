package com.vardemin.wiwing;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xavie on 21.05.2016.
 */
public class WiClient implements MenuActivityListener, Runnable {
    private static final String TAG = "CLIENT";

    private String hostaddress;
    private Socket socket;
    private WiMenu ctx;

    private UsersListener usersListener;
    private MessagesListener messagesListener;
    private DialogListener dialogListener;
    private NewsListener newsListener;

    private int RANG =-1;
    public void setRANG(int rang) { this.RANG = rang;}

    private User currentUser;
    private ClientSocketListener clientSocketListener;

    private String serverIp = "";
    public String getServerIp() {return serverIp;}

    private WiSync syncer;

    public WiClient(WiMenu ctx,String hostaddress) {
        this.hostaddress = hostaddress;
        this.ctx = ctx;
    }
    private int port = 8988;
    public void setPort(int port) { this.port = port;}

    public void setCurrentUser(User user){this.currentUser = user;}
    public void setAddress(WifiP2pInfo info) {this.hostaddress = hostaddress;}


    private ObjectOutputStream output;
    private ObjectInputStream input;
    /*@Override
    protected Void doInBackground(Void... params) {
        try {
            Log.d(TAG, " Waiting client connection to "+hostaddress+ " port: "+String.valueOf(port));
            socket = new Socket(hostaddress, port);
            Log.d(TAG,"connected");
            //socket.setKeepAlive(true);
            //socket.setReuseAddress(true);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            syncer = WiSync.getInstance();
            serverIp = socket.getInetAddress().getHostAddress();
            syncer.addConnection(serverIp,output);
            clientSocketListener = new ClientSocketListener();
            new Thread(clientSocketListener).start();
            Log.d(TAG, "started client socket listener on port "+String.valueOf(port)+ " from "+serverIp);
            if (currentUser!=null) {
                SendPacket(new WiWingPacket(WiPackageType.onEnter,currentUser));
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/


    public int SendPacket(WiWingPacket packet){
        try {
            output.writeObject(packet);
            output.flush();
            Log.d(TAG, "Sent object");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void onMenuActivityStop() {
        Thread.currentThread().interrupt();
        if(clientSocketListener!=null)
            clientSocketListener.onMenuActivityStop();

    }

    @Override
    public void run() {
        try {
            Log.d(TAG, " Waiting client connection to "+hostaddress+ " port: "+String.valueOf(port));
            socket = new Socket(hostaddress, port);
            Log.d(TAG,"connected");
            //socket.setKeepAlive(true);
            socket.setReuseAddress(true);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            syncer = WiSync.getInstance();
            serverIp = socket.getInetAddress().getHostAddress();
            syncer.addConnection(serverIp,output);
            clientSocketListener = new ClientSocketListener();
            new Thread(clientSocketListener).start();
            Log.d(TAG, "started client socket listener on port "+String.valueOf(port)+ " from "+serverIp);
            if (currentUser!=null) {
                if (currentUser.getRANG()==-1)
                SendPacket(new WiWingPacket(WiPackageType.onEnter,currentUser));
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientSocketListener implements Runnable, MenuActivityListener {
        private ObjectInput inpStream;
        private String ip;


        public ClientSocketListener() {
            try {
                inpStream = new ObjectInputStream(socket.getInputStream());
                this.ip = socket.getInetAddress().getHostAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    Log.d(TAG,"waiting for input for socket on "+String.valueOf(port)+" from " +serverIp);
                    WiWingPacket packet = (WiWingPacket) inpStream.readObject();
                    RecvPacket(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    onMenuActivityStop();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        private int RecvPacket(WiWingPacket object){
            String key ="";
            User user;
            switch (object.getType()){
                case onList:
                    user = object.getInstance(User.CREATOR);
                    key = object.getIp();
                    Log.d(TAG, "onList get ip: " + key);
                    Log.d(TAG,"Current user rang "+String.valueOf(currentUser.getRANG())+" user RANG: "+String.valueOf(user.getRANG()));
                    if (currentUser.getRANG()>user.getRANG()) {
                        WiClient client = new WiClient(ctx, key);
                        client.setPort(7715);
                        client.setRANG(currentUser.getRANG());
                        client.setCurrentUser(currentUser);
                        client.setUsersListener(ctx);
                        client.setNewsListener(ctx);
                        client.setDialogListener(ctx);
                        client.setMessagesListener(ctx);
                        ctx.addClient(client);
                        Log.d(TAG, "Executing client connection to " + key + " port: 7715");
                        new Thread(client).start();
                    }
                    syncer.addUser(key, user);
                    fireOnUserReceive(user);
                    break;
                case onResult:
                    user = object.getInstance(User.CREATOR);
                    syncer.addUser(serverIp, user);
                    fireOnUserReceive(user);
                    break;
                case onExit:
                    key = object.getIp();
                    user = syncer.getUser(key);
                    syncer.removeUser(key);
                    syncer.removeConnection(key);
                    fireOnUserExtir(user);
                    break;
                case onDialog:
                    Dialog dialog = object.getInstance(Dialog.CREATOR);
                    fireOnDialogCreate(dialog);
                    break;
                case onMessage:
                    Message message = object.getInstance(Message.CREATOR);
                    fireOnMessageReceive(message);
                    break;
                case onNews:
                    News news = object.getInstance(News.CREATOR);
                    fireOnNewsReceive(news);
                    break;
                case onEcho:
                    int RANG = Integer.parseInt(object.getIp());
                    currentUser.setRANG(RANG);
                    Log.d(TAG,"Received RANG: "+ String.valueOf(RANG));
                    break;
            }
            return 1;
        }


        @Override
        public void onMenuActivityStop() {
            Thread.currentThread().interrupt();
        }
    }

    public void setUsersListener(UsersListener usrListener) {this.usersListener = usrListener;}
    public void setDialogListener(DialogListener dialogListener) { this.dialogListener = dialogListener;}
    public void setMessagesListener(MessagesListener messagesListener) { this.messagesListener = messagesListener;}
    public void setNewsListener(NewsListener newsListener) {this.newsListener = newsListener;}

    private void fireOnUserReceive(User user){
        usersListener.onUserReceive(user);
    }
    private void fireOnUserExtir(User user) {usersListener.onUserExit(user);}
    private void fireOnDialogCreate(Dialog dialog) { dialogListener.onDialogCreate(dialog);}
    private void fireOnNewsReceive(News news) { newsListener.onNewsReceive(news);}
    private void fireOnMessageReceive(Message message) { messagesListener.onMessageReceive(message);}


}
