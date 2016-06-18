package com.vardemin.wiwing;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xavie on 20.05.2016.
 */
public class WiServer implements MenuActivityListener, Runnable {
    private static final String TAG = "WIWING SERVER";

    private MessagesListener messagesListener;
    private UsersListener usersListener;
    private NewsListener newsListener;
    private DialogListener dialogListener;

    private ServerSocket serverSocket;
    public static final int SERVERPORT = 8988;
    public static final String SERVER_IP = "192.168.49.1";
    private WiSync syncer;

    private SocketListener socketListener;

    private WiMenu ctx;
    private int port = 8988;

    public WiServer(WiMenu ctx,User currentUser){
        this.ctx = ctx;
        syncer = WiSync.getInstance();
    }
    public WiServer() {syncer = WiSync.getInstance();}
    private static WiServer server = new WiServer();
    public static synchronized WiServer getServer() {
        return server;
    }
    public static synchronized void ClearInstance() { server=new WiServer();}
    public void setCtx(WiMenu ctx){this.ctx = ctx;}
    public void setPort(int port) {this.port = port;}

    private boolean isAvailable= true;
    public boolean isAvailable() { return isAvailable;}



    @Override
    public void run() {
        Socket socket = null;

        Log.d(TAG, "STARTED");
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            isAvailable = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!Thread.currentThread().isInterrupted()){
            try {
                Log.d(TAG, "Waiting for connection on port"+String.valueOf(port));
                socket = serverSocket.accept();
                //socket.setKeepAlive(true);
                socket.setReuseAddress(true);
                Log.d(TAG, "RECEIVED CONNECTION ON PORT " + String.valueOf(port));

                socketListener = new SocketListener(socket, socket.getInetAddress().getHostAddress());
                syncer.addConnection(socket.getInetAddress().getHostAddress(),new ObjectOutputStream(socket.getOutputStream()));
                new Thread(socketListener).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isAvailable = true;
    }

    /*@Override
    protected String doInBackground(Void... params) {
        Socket socket = null;

        Log.d(TAG, "STARTED");
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!this.isCancelled()){
            try {
                Log.d(TAG, "Waiting for connection on port"+String.valueOf(port));
                socket = serverSocket.accept();
                //socket.setKeepAlive(true);
                socket.setReuseAddress(true);
                Log.d(TAG, "RECEIVED CONNECTION ON PORT " + String.valueOf(port));

                socketListener = new SocketListener(socket, socket.getInetAddress().getHostAddress());
                syncer.addConnection(socket.getInetAddress().getHostAddress(),new ObjectOutputStream(socket.getOutputStream()));
                new Thread(socketListener).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "SHUTDOWN SERVER");
    }*/


    @Override
    public void onMenuActivityStop() {
        if (socketListener!=null)
            socketListener.onMenuActivityStop();
        isAvailable = true;
        Thread.currentThread().interrupt();
        /*if(socketListener!=null)
            socketListener.onMenuActivityStop();
        if (serverSocket!=null)
        try {
            serverSocket.close();
            this.cancel(true);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }




    class SocketListener implements Runnable, MenuActivityListener{

        private Socket clSock;
        private ObjectInputStream inpStream = null;
        private ObjectOutputStream outStream = null;
        private String ipListen;

        public SocketListener(Socket clientSocket,  String key) {
            this.clSock = clientSocket;
            ipListen = key;
            try {
                this.inpStream = new ObjectInputStream(this.clSock.getInputStream());
                this.outStream = new ObjectOutputStream(this.clSock.getOutputStream());
            } catch (IOException e) {
                onMenuActivityStop();
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    try {
                        WiWingPacket wiPacket = (WiWingPacket) inpStream.readObject();
                        ReadInputObject(wiPacket);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }catch (IOException e) {
                    e.printStackTrace();
                    onMenuActivityStop();
                }

            }
        }

        private int ReadInputObject(WiWingPacket object){

            switch (object.getType()){
                case onEnter:
                    User user = object.getInstance(User.CREATOR);
                    syncer.addUser(clSock.getInetAddress().getHostAddress(), user);
                    fireOnUsersReceive(user);
                    Log.d(TAG, "got user: name - " + user.getName());
                    echoUserRank();
                    responseUser();
                    broadcastUserList();

                    break;

                case onExit:
                    fireOnUserExit(syncer.getUser(ipListen));
                    syncer.removeUser(ipListen);
                    syncer.removeConnection(ipListen);
                    syncer.broadcastExit(ipListen);
                    break;

                case onNews:
                    News news = object.getInstance(News.CREATOR);
                    fireOnNewsReceive(news);
                    break;

                case onList:
                    User listed_user = object.getInstance(User.CREATOR);
                    String key = object.getIp();
                    syncer.addUser(key, listed_user);
                    fireOnUsersReceive(listed_user);

                    break;

                case onDialog:
                    Dialog dialog = object.getInstance(Dialog.CREATOR);
                    fireOnDialogCreate(dialog);
                    break;

                case onMessage:
                    Message message = object.getInstance(Message.CREATOR);
                    fireOnMessageReceive(message);
                    break;
            }

            return 1;
        }


        private void broadcastUserList() {
            syncer.broadcastList();
        }

        private void responseUser() {
            try {
                User curUsr = ctx.getCurrentUser();
                curUsr.setRANG(0);
                outStream.writeObject(new WiWingPacket(WiPackageType.onResult,curUsr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void echoUserRank() {
            try {
                outStream.writeObject(
                        new WiWingPacket(WiPackageType.onEcho, String.valueOf(syncer.getUser(ipListen).getRANG())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onMenuActivityStop() {
            Thread.currentThread().interrupt();
            /*if (clSock!=null)
                try {
                    usersIpDict.remove(clSock.getInetAddress().getHostAddress());
                    clSock.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            */
        }


    }

    public void setMessagesListener(MessagesListener msgListener) {
        this.messagesListener = msgListener;
    }

    public void setUsersListener(UsersListener usersListener){
        this.usersListener = usersListener;
    }

    public void setNewsListener(NewsListener nwsListener) {
        this.newsListener = nwsListener;
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    private void fireOnMessageReceive(Message msg) {
        messagesListener.onMessageReceive(msg);
    }
    private void fireOnNewsReceive(News news) {
        newsListener.onNewsReceive(news);
    }
    private void fireOnUsersReceive(User user) { usersListener.onUserReceive(user);}
    private void fireOnUserExit (User user) { usersListener.onUserExit(user);}
    private void fireOnDialogCreate(Dialog dialog) { dialogListener.onDialogCreate(dialog);}
}
    /*private int ProcessPacket(WiPackageType type) {
            Log.d(TAG," Processing "+type.toString() + " package");
            switch (type) {
                case onEnter:
                    try {
                        byte[] byteBuf;
                        int pktCount = inpStream.readInt();
                        byteBuf = new byte[pktCount];
                        if (inpStream.read(byteBuf)!=-1) {
                            User user = ParcelableUtility.unmarshall(byteBuf,User.CREATOR);
                            syncer.addUser(clSock.getInetAddress().getHostAddress(), user);
                            onEnter(clSock.getInetAddress().getHostAddress(), user);
                            Log.d(TAG,"got user: name - "+user.getName());
                            return 1;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }break;

                case onExit:
                    syncer.removeUser(clSock.getInetAddress().getHostAddress());
                    syncer.removeConnection(clSock.getInetAddress().getHostAddress());
                    return onList();

                case onList:
                    List<User> users = syncer.getUsersList();
                    for (Map.Entry<String, User> userEntry : usersDictionary.entrySet()) {
                        try {
                            outStream.writeUTF(userEntry.getKey());
                            byte[] bytes = ParcelableUtility.marshall(userEntry.getValue());
                            outStream.writeInt(bytes.length);
                            outStream.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return -1;
                        }
                    }return 1;

                case onMesage:
                    try {
                        byte[] byteBuf;
                        int pktCount = inpStream.readInt();
                        byteBuf = new byte[pktCount];
                        if (inpStream.read(byteBuf)!=-1) {
                            Message msg = ParcelableUtility.unmarshall(byteBuf,Message.CREATOR);
                            fireOnMessageReceive(msg);
                            return 1;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }break;
                case onNews:
                    try {
                        byte[] byteBuf;
                        byteBuf = new byte[inpStream.readInt()];
                        if (inpStream.read(byteBuf)!=-1) {
                            News news = ParcelableUtility.unmarshall(byteBuf, News.CREATOR);
                            fireOnNewsReceive(news);
                            return 1;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }break;
            }
            return -1;
        }
        private void onEnter(String key, User user) {
            WiPackageType pktType = WiPackageType.onEnter;
            int type = pktType.ordinal();
            for (Map.Entry<String, DataOutputStream> entry : usersIpDict.entrySet())
            {
                if (!entry.getKey().equals(key)){
                    try {
                        entry.getValue().writeInt(type);
                        entry.getValue().writeUTF(entry.getKey());
                        byte[] bytes = ParcelableUtility.marshall(user);
                        entry.getValue().writeInt(bytes.length);
                        entry.getValue().write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private int onList() {
            WiPackageType pktType = WiPackageType.onList;
            int type = pktType.ordinal();
            for (Map.Entry<String, DataOutputStream> streamEntry : usersIpDict.entrySet())
            {
                try {
                    streamEntry.getValue().writeInt(type);
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
                for (Map.Entry<String, User> userEntry : usersDictionary.entrySet()) {
                    try {
                        streamEntry.getValue().writeUTF(userEntry.getKey());
                        byte[] bytes = ParcelableUtility.marshall(userEntry.getValue());
                        streamEntry.getValue().writeInt(bytes.length);
                        streamEntry.getValue().write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return -1;
                    }
                }
            }
            return 1;
        }*/

