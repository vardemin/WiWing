package com.vardemin.wiwing;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by xavie on 16.05.2016.
 */
public class User implements Parcelable {
    private String          uuid;
    private String          name;
    private int             age;
    private String          status;
    private byte[]          photo;
    private int             RANG = -1;

    public User(String uuid, String name, int age, String status, byte[] photo) {
        this.uuid = uuid;
        this.name = name;
        this.age = age;
        this.status = status;
        this.photo = photo;
    }
    public void setRANG(int rang) { this.RANG = rang;}
    public int getRANG() {return RANG;}

    public String getUuid() {return uuid;}
    public void setUuid(String uuid) {this.uuid = uuid;}

    public String getName() { return name; }
    public void   setName(String name) { this.name = name; }

    public int    getAge() { return age; }
    public void   setAge(int age) { this.age = age; }

    public String getStatus() { return status; }
    public void   setStatus(String status) { this.status = status; }

    public byte[] getPhoto() { return photo;}
    public void setPhoto(byte[] photo) { this.photo = photo;}

    @Override
    public int describeContents() { return 0;}

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(uuid);
        out.writeString(name);
        out.writeInt(age);
        out.writeString(status);
        out.writeByteArray(photo);
    }
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private User(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        age = in.readInt();
        status = in.readString();
        photo = in.createByteArray();
    }
}
