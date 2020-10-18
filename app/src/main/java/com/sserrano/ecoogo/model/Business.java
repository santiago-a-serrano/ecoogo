package com.sserrano.ecoogo.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class Business {
    private String name;
    private int type;
    private String typeName;
    private int[] tags;
    private String[] tagNames;
    private double latitude, longitude;
    private LatLng location;
    private String address;
    private String hours = null;
    private String webpage = null;
    private String phone = null;
    private String[] imageURLs = null;
    private int rating = 0;

    public Business(@NonNull String name, @NonNull int type, String typeName, @NonNull int[] tags,
                    String[] tagNames, @NonNull double latitude, @NonNull double longitude,
                    String address, int rating){
        this.name = name;
        this.type = type;
        this.typeName = typeName;
        this.tags = tags;
        this.tagNames = tagNames;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.rating = rating;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public void setWebpage(String webpage) {
        this.webpage = webpage;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImageURLs(String[] imageURLs) {
        this.imageURLs = imageURLs;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int[] getTags() {
        return tags;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLocation(){
        return new LatLng(latitude, longitude);
    }

    public String getAddress(){
        return address;
    }

    public String getTypeName() {
        return typeName;
    }

    public String[] getTagNames() {
        return tagNames;
    }

    public String getHours() {
        return hours;
    }

    public String getWebpage() {
        return webpage;
    }

    public String getPhone() {
        return phone;
    }

    public String[] getImageURLs() {
        return imageURLs;
    }

    public int getRating() {
        return rating;
    }
}
