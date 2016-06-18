package com.vardemin.wiwing;

import android.util.Log;

import com.vardemin.wiwing.models.UserModel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by xavie on 27.05.2016.
 */
public class WiSync {
    private static final String TAG="WiSync";

    private ConcurrentMap<String,ObjectOutputStream> connectMap;
    private ConcurrentMap<String,User> usersMap;

    private static WiSync curInstance = new WiSync();
    public static WiSync getInstance() {
        return curInstance;
    }

    private WiSync() {
        connectMap = new ConcurrentHashMap<>();
        usersMap = new ConcurrentHashMap<>();
    }

    public void addConnection(String key, ObjectOutputStream value) {
        connectMap.putIfAbsent(key, value);
    }
    public void removeConnection(String key) {
        connectMap.remove(key);
    }
    public ObjectOutputStream getStream(String key) {
        return connectMap.get(key);
    }
    public List<ObjectOutputStream> getAllStream() {
        List<ObjectOutputStream> streamList = new ArrayList<>();
        streamList.addAll(connectMap.values());
        return streamList;
    }

    public void addUser(String key, User value) {
        if(value.getRANG()==-1){
            value.setRANG(usersMap.size()+1);}
        usersMap.putIfAbsent(key, value);
    }
    public void removeUser(String key) {
        usersMap.remove(key);
    }
    public User getUser (String key){
        return usersMap.get(key);
    }
    public List<User> getUsersList (){
        List<User> userList= new ArrayList<>();
        userList.addAll(usersMap.values());
        return userList;
    }

    public synchronized int broadcastList() {
        for(Map.Entry<String, ObjectOutputStream> stream:connectMap.entrySet()){
            for (Map.Entry<String, User> user:usersMap.entrySet()) {
                try {
                    if (stream.getKey().equals(user.getKey()))
                        continue;
                    stream.getValue().writeObject(new WiWingPacket(WiPackageType.onList, user.getValue(), user.getKey()));
                    stream.getValue().flush();
                    stream.getValue().reset();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        return 1;
    }

    public synchronized int broadcastExit(String key){
        for(Map.Entry<String, ObjectOutputStream> stream: connectMap.entrySet()){
            try {
                stream.getValue().writeObject(new WiWingPacket(WiPackageType.onExit, key));
                stream.getValue().flush();
                stream.getValue().reset();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 1;
    }

    public synchronized int broadcastPacket(WiWingPacket packet) {
        for (ObjectOutputStream stream: connectMap.values())
            try {
                stream.writeObject(packet);
                stream.flush();
                stream.reset();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        return 1;
    }

    public int sendToList (List<User> users, WiWingPacket packet) {
        for (Map.Entry<String, ObjectOutputStream> stream: connectMap.entrySet()) {
            String streamUserUUID = usersMap.get(stream.getKey()).getUuid();
            for (User user: users)
            {
                if (user.getUuid().equals(streamUserUUID))
                {
                    try {
                        stream.getValue().writeObject(packet);
                        stream.getValue().flush();
                        stream.getValue().reset();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }

        }
        return 1;
    }

    public void clearAll() {
        usersMap.clear();
        connectMap.clear();
    }

    public CharSequence[] getUsersNamesList() {
        List<String> names = new ArrayList<>();
        for (User user: usersMap.values())
            names.add(user.getName());
        return names.toArray(new CharSequence[names.size()]);
    }



}
