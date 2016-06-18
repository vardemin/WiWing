package com.vardemin.wiwing;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by xavie on 23.05.2016.
 */
public class WiWingPacket implements Serializable {
    private WiPackageType type;
    private byte[] instance = null;
    private String ip = null;

    public WiWingPacket (WiPackageType type){
        this.type = type;
    }

    public WiWingPacket(WiPackageType type, Parcelable parcel){
        this.type = type;
        this.instance = ParcelableUtility.marshall(parcel);
    }
    public WiWingPacket(WiPackageType type, Parcelable parcel, String ip){
        this.type = type;
        this.instance = ParcelableUtility.marshall(parcel);
        this.ip = ip;
    }
    public WiWingPacket(WiPackageType type, String ip){
        this.type = type;
        this.ip = ip;
    }

    public String getIp() {return this.ip;}

    public void setType(WiPackageType type) {this.type=type;}
    public WiPackageType getType() {return type;}

    public void setInstance(Parcelable parcelable) {
        this.instance = ParcelableUtility.marshall(parcelable);
    }

    public <T extends Parcelable> T getInstance(Parcelable.Creator<T> creator){
        return ParcelableUtility.unmarshall(instance,creator);
    }
}
