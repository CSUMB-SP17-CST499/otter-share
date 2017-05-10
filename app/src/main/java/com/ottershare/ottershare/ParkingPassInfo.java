package com.ottershare.ottershare;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


/**
 * Created by ryan on 5/6/17.
 */

public class ParkingPassInfo implements Parcelable{

    private String id;
    private LatLng gpsLocation;
    private String notes;
    private boolean forSale;
    private float price;
    private int lotLocation;
    private String email;

    public ParkingPassInfo( String id,
                          LatLng gpsLoction,
                          String notes,
                          boolean forSale,
                          float price,
                          int lotLocation,
                          String email){

        this.id = id;
        this.gpsLocation = gpsLoction;
        this.notes = notes;
        this.forSale = forSale;
        this.price = price;
        this.lotLocation = lotLocation;
        this.email = email;

    }

    //getter methods.
    public String getId() {
        return id;
    }

    public LatLng getGpsLoction() {
        return gpsLocation;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isForSale() {
        return forSale;
    }

    public float getPrice() {
        return price;
    }

    public int getLotLocation() {
        return lotLocation;
    }

    public String getEmail() {
        return email;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    public ParkingPassInfo(Parcel p){
        id = p.readString();
        gpsLocation = new LatLng(p.readDouble(),p.readDouble());
        notes = p.readString();
        forSale = p.readByte() != 0;
        price = p.readFloat();
        lotLocation = p.readInt();
        email = p.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(gpsLocation.latitude);
        dest.writeDouble(gpsLocation.longitude);
        dest.writeString(getNotes());
        dest.writeByte((byte) (isForSale()? 1:0));
        dest.writeFloat(getPrice());
        dest.writeInt(getLotLocation());
        dest.writeString(getEmail());

    }

    public static final Creator<ParkingPassInfo> CREATOR = new Creator<ParkingPassInfo>() {
        @Override
        public ParkingPassInfo createFromParcel(Parcel parcel) {
            return new ParkingPassInfo(parcel);
        }

        @Override
        public ParkingPassInfo[] newArray(int i) {
            return new ParkingPassInfo[0];
        }
    };
}
