package com.periphery.littlefreelibrary;

import android.location.Location;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Charter implements Serializable {

    String cid;
    String address;
    String longAddress;
    String number;
    String steward;
    String email;
    double lat;
    double longitude;
    double distanceDouble;
    String distanceString;
    boolean favorite;
    String image;
    int imagefull;
    String description;
    public ArrayList<Book> books;


    public Charter(String cid, String address, String restOfAddress, String number, String steward, String email, double lat, double longitude ,double lat2, double long2, String image, String description)
    {
        this.cid = cid;
        this.address = address;
        this.longAddress = address + " " + restOfAddress;
        this.description = description;
        this.number = "Charter #" + number;
        this.steward = steward;
        this.email = email;
        this.lat = lat;
        this.longitude = longitude;
        this.distanceDouble = calculateDistance(this.lat,this.longitude,lat2,long2);
        this.distanceString = round(this.distanceDouble);//Double.toString(distanceDouble).substring(0,4) + " mi";
        //this.distanceDouble = Math.random() * 5;
        //this.distanceString = Double.toString(distanceDouble).substring(0,4) + " mi";
        this.favorite = false;
        this.image = image;
        this.imagefull = imagefull;
        this.books = new ArrayList<>();
    }

    public String getCid() {
        return this.cid;
    }

    public String getImageURL() {
        String base = "https://littlefreelibrary.secure.force.com/servlet/servlet.FileDownload?file=";
        return base + image;
    }
    public String getAddress(){
        return address;
    }
    public String getNumber(){
        return number;
    }

    public double calculateDistance(double lat1, double lon1, double lat2,
                                    double lon2) {

        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        double distance = loc1.distanceTo(loc2);

        return distance * 0.000621371; // convert to miles
    }

    public String getDistance() {
        return this.distanceString;
    }

    public void addBook(Book book) {
        if (book != null) {
            books.add(0,book);
        }
    }
    public void setDistanceDoubleandString(double lat2,
                                  double lon2){
        this.distanceDouble = calculateDistance(this.lat, this.longitude, lat2, lon2);
        this.distanceString = round(this.distanceDouble);//Double.toString(distanceDouble).substring(0,4) + " mi";
    }
    public void setFavorite(boolean bool)
    {
        favorite = bool;
    }
    public boolean getFavorite()
    {
        return this.favorite;
    }
    public static String round(double value)
    {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        double d =  bd.doubleValue();
        return Double.toString(d) + " mi";
    }
}