package com.vardemin.wiwing;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by xavie on 28.05.2016.
 */
public class Dialog implements Parcelable {
    private String name;
    private String uuid;
    private List<String> usersUUIDList;

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getUUID() { return uuid;}
    public List<String> getUsersUUIDList() {return usersUUIDList;}

    public Dialog(String name,String uuid, List<String> usersUUIDList){
        this.name = name;
        this.uuid = uuid;
        this.usersUUIDList = usersUUIDList;
    }

    protected Dialog(Parcel in) {
        name = in.readString();
        uuid = in.readString();
        usersUUIDList = in.createStringArrayList();
    }

    public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
        @Override
        public Dialog createFromParcel(Parcel in) {
            return new Dialog(in);
        }

        @Override
        public Dialog[] newArray(int size) {
            return new Dialog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uuid);
        dest.writeStringList(usersUUIDList);
    }


}
