package com.vardemin.wiwing.models;

import com.vardemin.wiwing.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by xavie on 16.05.2016.
 */
public class UserModel extends RealmObject {
    @Required
    @PrimaryKey
    private String          uuid;
    private String          name;
    private String          password;
    private int             age;
    private String          status;
    private byte[]          photo;
    private boolean         isLocal = false;
    private RealmList<UserModel> friends;

    public UserModel() {
        this.dialogs = new RealmList<>();
        this.friends = new RealmList<>();
    }

    public RealmList<DialogModel> getDialogs() {
        return dialogs;
    }

    public void setDialogs(RealmList<DialogModel> dialogs) {
        this.dialogs = dialogs;
    }
    public void addDialog(DialogModel model) {
        this.dialogs.add(model);
    }

    private RealmList<DialogModel> dialogs;

    public String getUuid() {return uuid;}
    public void setUuid() {this.uuid = UUID.randomUUID().toString();}
    public void setUuid(String uuid) { this.uuid = uuid;}

    public String getName() { return name; }
    public void   setName(String name) { this.name = name; }

    public int    getAge() { return age; }
    public void   setAge(int age) { this.age = age; }

    public String getStatus() { return status; }
    public void   setStatus(String status) { this.status = status; }

    public byte[] getPhoto() { return photo;}
    public void setPhoto(byte[] photo) { this.photo = photo;}

    public boolean isLocal() {return isLocal;}
    public void setIsLocal(boolean isLocal) {this.isLocal = isLocal;}

    public void addFriend(UserModel friend){
        this.friends.add(friend);
    }
    public void removeFriend(UserModel friend) {
        this.friends.remove(friend);
    }

    public User getUser() {
        return new User(this.uuid, this.name, this.age, this.status, this.photo);
    }

    public static UserModel getModel(User user) {
        UserModel model = new UserModel();
        model.setUuid(user.getUuid());
        model.setName(user.getName());
        model.setAge(user.getAge());
        if (user.getPhoto()!=null)
            model.setPhoto(user.getPhoto());
        if (user.getStatus()!=null)
            model.setStatus(user.getStatus());
        return model;
    }


    public String getPassword() {
        return password;
    }
    public void setPassword(String password){
        this.password = getPasswordInstance(password);
    }

    public boolean ComparePassword(String password) {
        if (getPasswordInstance(password).equals(this.password))
            return true;
        else return false;
    }

    public String getPasswordInstance(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return password;
    }
}
