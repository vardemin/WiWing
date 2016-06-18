package com.vardemin.wiwing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xavie on 16.05.2016.
 */
public class Message implements Parcelable {
    private String dialogUUID;
    private String name;
    private String message;
    private long dateTime;

    public Message(String dialogUUID, String name, String message, long dateTime) {
        this.dialogUUID = dialogUUID;
        this.name = name;
        this.message = message;
        this.dateTime = dateTime;
    }

    public long getDateTime() {return dateTime;}
    public void setDateTime(long dateTime) {this.dateTime = dateTime;}

    public String getDialogUUID() {return dialogUUID;}
    public void setDialogUUID(String dialogUUID) {this.dialogUUID = dialogUUID;}

    public String getName() { return name; }
    public void setName(String name) { this.name = name;}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message;}


    @Override
    public int describeContents() {return 0;}

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(dialogUUID);
        out.writeString(name);
        out.writeString(message);
        out.writeLong(dateTime);
    }
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {return new Message(in);}
        @Override
        public Message[] newArray(int size) {return new Message[size];}
    };
    protected Message(Parcel in) {
        dialogUUID = in.readString();
        name = in.readString();
        message = in.readString();
        dateTime = in.readLong();
    }


}

