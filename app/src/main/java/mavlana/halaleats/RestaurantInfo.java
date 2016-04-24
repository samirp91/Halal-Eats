package mavlana.halaleats;

import android.location.Location;

import java.text.DecimalFormat;

/**
 * Created by samir on 13/09/15.
 */
public class RestaurantInfo implements Comparable<RestaurantInfo> {
    public String getrID() {
        return rID;
    }

    public void setrID(String rID) {
        this.rID = rID;
    }

    private String rID;
    private String name;
    private String address;
    private String phoneNumber;

    private double lng;
    private double lat;

    private String distance;
    private String website;

    private Boolean favourite;

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    private String cuisine;
    private String price;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private String city;

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public RestaurantInfo(String [] info){
        try {
            this.setrID(info[0]);
            this.setName(info[1]);
            this.setAddress(info[2]);
            this.setPhoneNumber(info[3]);
            this.setLat(Double.valueOf(info[4]));
            this.setLng(Double.valueOf(info[5]));
            this.setWebsite(info[6]);
            this.setCity(info[7]);
            this.setCuisine(info[8]);
            this.setPrice(info[9].trim());
            this.setTime(info[10]);
            this.favourite = false;
        }
        catch (Exception e){
            System.out.println(this.getName());
        }
    }
    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    public void setDistance(String distance) { this.distance = distance; }

    public String getDistance() { return this.distance; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String priceTitle(){
        if (this.getPrice().contains("$ (entree under $10)")){
            return "$";
        }
        else if (this.getPrice().contains("$$ (entree $10-20)")){
            return "$$";
        }
        else if (this.getPrice().contains("$$$ (entree $20-30)")){
            return "$$$";
        }
        else{
            System.out.println(getName());
            return "Price range not found";
        }
    }

    public String cuisineString(){
        String[] cuisineSplit;
        String cuisineStr;
        cuisineSplit = this.getCuisine().split("\\+");
        int count = cuisineSplit.length;
        if (count == 1){
            return cuisineSplit[0];
        }
        cuisineStr = cuisineSplit[0];
        for (int i = 1; i < count; i++){
            cuisineStr = cuisineStr + ", " + cuisineSplit[i];
        }

        return cuisineStr.trim();

    }
    public String timeToString(){
        String[] timeSplit;
        String timeFinal = "";

        if (this.getTime() != null && this.getTime().contains("\\\\")){
            timeSplit = this.getTime().split("\\\\");
            for (String a : timeSplit){
                if (a.trim().equals("")){
                    continue;
                }
                timeFinal = timeFinal + a + "\n";
            }
            timeFinal = timeFinal.trim();
        }
        else{
            timeFinal = this.getTime();
        }
        return timeFinal;
    }

    public String toString(){
        return this.getName() + " - " +  this.priceTitle() + "\n" + this.cuisineString() + "\n" + this.getAddress() + "\n" + this.getPhoneNumber() + "\n" + this.getDistance() + " km";
    }


    @Override
    public int compareTo(RestaurantInfo r) {
        return Double.valueOf(this.getDistance()).compareTo(Double.valueOf(r.getDistance()));
    }

    public void updateDistance(double lat, double lng){
        float [] resultArray  = new float [99];
        Location.distanceBetween(lat, lng, this.getLat(), this.getLng(), resultArray);
        this.setDistance(new DecimalFormat("##.#").format(resultArray[0]/1000));
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

}
