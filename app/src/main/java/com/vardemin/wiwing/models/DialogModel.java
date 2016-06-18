package com.vardemin.wiwing.models;

import com.vardemin.wiwing.Dialog;
import com.vardemin.wiwing.Message;
import com.vardemin.wiwing.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by xavie on 16.05.2016.
 */
public class DialogModel extends RealmObject{

    private String uuid;
    private String name;
    private RealmList<UserModel> users;
    private RealmList<MessageModel> messages;

    public DialogModel(String UUID){
        this.uuid = UUID;
        users= new RealmList<>();
        messages = new RealmList<>();
    }
    public DialogModel(){
        this.uuid = UUID.randomUUID().toString();
        users = new RealmList<>();
        messages = new RealmList<>();
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getUUID() { return uuid;}
    public void setUUID() { uuid = UUID.randomUUID().toString();}

    public void addUser(UserModel user) {
        users.add(user);
    }
    public void addUsers(List<UserModel> users) { this.users.addAll(users);}
    public void removeUser(UserModel user) {
        users.remove(user);
    }

    public void addMessage (MessageModel message) {
        messages.add(message);
    }
    public void addMessages (List<MessageModel> messages) {
        this.messages.addAll(messages);
    }
    public void removeMessage(MessageModel message) {
        this.messages.remove(message);
    }

    public List<MessageModel> getMessagesModels() {
        return messages;
    }
    public List<UserModel> getUsersModels() {
        return users;
    }

    public List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        for (UserModel model: users)
            userList.add(model.getUser());
        return userList;
    }
    public List<Message> getMessages() {
        List<Message> messagesList = new ArrayList<>();
        for (MessageModel model: messages)
            messagesList.add(model.getMessage());
        return messagesList;
    }

    public Dialog getDialog() {
        List<String> usersUUID = new ArrayList<>();
        for (UserModel model: users)
            usersUUID.add(model.getUuid());
        return new Dialog(name,uuid,usersUUID);
    }


}
