package com.vardemin.wiwing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xavie on 16.05.2016.
 */

/*
* type: 0 - none
*       1 - photo
*       2 - audio
*       3 - video
* */
public class News implements Parcelable {
    private String userUUID;
    private String header;
    private String content;
    private byte[] addition;
    private byte type;

    public News(String userUUID, String header, String content, byte[] addition, byte type) {
        this.userUUID = userUUID;
        this.header = header;
        this.content = content;
        this.addition = addition;
        this.type = type;
    }
    public News(String userUUID,String header, String content){
        this.userUUID = userUUID;
        this.header = header;
        this.content = content;
        this.type = 0;
    }


    public String getUserUUID() {return userUUID;}
    public void setUserUUID(String userUUID) { this.userUUID = userUUID;}

    public byte[] getAddition() {return addition;}
    public void setAddition(byte[] addition) {this.addition = addition;}

    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}

    public String getHeader() {return header;}
    public void setHeader(String header) {this.header = header;}

    public byte getType() {return type;}
    public void setType(byte type) {this.type = type;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userUUID);
        dest.writeString(header);
        dest.writeString(content);
        dest.writeByteArray(addition);
        dest.writeByte(type);
    }
    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
    protected News(Parcel in) {
        userUUID = in.readString();
        header = in.readString();
        content = in.readString();
        addition = in.createByteArray();
        type = in.readByte();
    }
}
