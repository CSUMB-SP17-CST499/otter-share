package com.ottershare.ottershare;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ryan on 5/6/17.
 */

public class ParkingPassInfo {

    private String id;
    private LatLng gpsLoction;
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
        this.gpsLoction = gpsLoction;
        this.notes = notes;
        this.forSale = forSale;
        this.price = price;
        this.lotLocation = lotLocation;
        this.email = email;

    }

    public String getId() {
        return id;
    }

    public LatLng getGpsLoction() {
        return gpsLoction;
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

}
