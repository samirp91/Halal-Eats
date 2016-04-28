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
    private String restaurantName;
    private String address;
    private String phoneNumber;

    private double longitude;
    private double latitude;

    private String distance;
    private String website;

    private String cuisine;
    private String price;

    private Boolean favourite;

    private String time;

    private String city;

    public RestaurantInfo(String [] info){
        try {
            this.setrID(info[0]);
            this.setName(info[1]);
            this.setAddress(info[2]);
            this.setPhoneNumber(info[3]);
            this.setLatitude(Double.valueOf(info[4]));
            this.setLongitude(Double.valueOf(info[5]));
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

    public RestaurantInfo(){} //Need this for firebase to work.

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setDistance(String distance) { this.distance = distance; }

    public String getDistance() { return this.distance; }

    public String getName() {
        return restaurantName;
    }

    public void setName(String name) {
        this.restaurantName = name;
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
        Location.distanceBetween(lat, lng, this.getLatitude(), this.getLongitude(), resultArray);
        this.setDistance(new DecimalFormat("##.#").format(resultArray[0]/1000));
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

}
