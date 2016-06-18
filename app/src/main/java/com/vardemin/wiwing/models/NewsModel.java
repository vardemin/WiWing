package com.vardemin.wiwing.models;

import com.vardemin.wiwing.News;

import io.realm.RealmObject;

/**
 * Created by xavie on 16.05.2016.
 */
/*
* type: 0 - none
*       1 - photo
*       2 - audio
*       3 - video
* */
public class NewsModel extends RealmObject {
    private String userUUID;
    private String header;
    private String content;
    private byte[] addition;
    private byte type;

    public NewsModel(String userUUID,String header, String content, byte[] addition, byte type) {
        this.userUUID = userUUID;
        this.header = header;
        this.content = content;
        this.addition = addition;
        this.type = type;
    }
    public NewsModel() {
        this.type = 0;
    }
    public NewsModel(String userUUID){
        this.userUUID = userUUID;
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

    public News getNew() {
        if (this.addition!=null)
            return new News(this.userUUID,this.header,this.content,this.addition,this.type);
        else return new News(this.userUUID,this.header, this.content);
    }
}
