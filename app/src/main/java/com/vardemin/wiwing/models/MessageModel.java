package com.vardemin.wiwing.models;

import com.vardemin.wiwing.Message;

import io.realm.RealmObject;

/**
 * Created by xavie on 16.05.2016.
 */
public class MessageModel extends RealmObject {
    private String dialogUUID;
    private String name;
    private String message;
    private long dateTime;

    public MessageModel(String dialogUUID, String name, String message, long dateTime) {
        this.dialogUUID = dialogUUID;
        this.name = name;
        this.message = message;
        this.dateTime = dateTime;
    }
    public MessageModel() {}

    public long getDateTime() {return dateTime;}
    public void setDateTime(long dateTime) {this.dateTime = dateTime;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name;}

    public String getMessages() { return message; }
    public void setMessage(String message) { this.message = message;}

    public String getDialogUUID() {return dialogUUID;}
    public void setDialogUUID(String dialogUUID) {this.dialogUUID = dialogUUID;}

    public Message getMessage() {
        return new Message(dialogUUID,name,message,dateTime);
    }
}
