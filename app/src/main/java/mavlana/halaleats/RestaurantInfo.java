package mavlana.halaleats;

import android.location.Location;

import java.text.DecimalFormat;
import java.util.Calendar;

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

    private String status;
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
            if (this.getTime().equals("Timings Not Available"))
                this.setStatus("TIMINGS NOT AVAILABLE");
            else if (isOpen())
                this.setStatus("OPEN");
            else
                this.setStatus("CLOSED");
        }
        catch (Exception e){
            System.out.println(this.getName());
        }
    }

    public RestaurantInfo(){} //Need this for firebase to work.

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }
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
            //System.out.println(getName());
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
//        System.out.println(this.getName());
        return this.getName() + " - " +  this.priceTitle() + "\n" + this.cuisineString() + "\n" + this.getAddress() + "\n" + this.getPhoneNumber() + "\n" + this.getDistance() + " km\n" + this.getStatus();
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

    public boolean isOpen(){
//        System.out.println(this.getName());
        if (this.getTime().equals("Timings Not Available"))
            return true;

        String[] preprocess = this.timeToString().split("\n");
        String todaysTime = getTodaysTime(preprocess);
        if (todaysTime.trim().equals("Closed"))
            return false;

        double timeNow = getTimeNow();

        String[] times = todaysTime.split("&");
        for (String time : times) {
//            System.out.println(time);
            double[] timeRange = getTimeRange(time.trim());
//            System.out.println(timeRange[0]);
//            System.out.println(timeRange[1]);
//            System.out.println(timeNow);
            if (timeRange[0] == 0.0 && timeRange[1] == 23.59) { //24 hours
                return true;
            }
            if (timeRange[0] <= timeNow && timeNow <= timeRange[1])
                return true;
        }

        return false;
    }

    public double[] getTimeRange(String todaysTime) {
        String[] timeSplit = todaysTime.split(" - ");
        String start = timeSplit[0].trim();
        String end = timeSplit[1].trim();

        double startHour = 0;
        double startMinute = 0;
        double endHour = 0;
        double endMinute = 0;

        String[] startRange = start.split(".m");
        String[] endRange = end.split(".m");

        if (startRange[0].contains(":")) {
            String[] range = startRange[0].split(":");
            startHour = Double.valueOf(range[0]);
            startMinute = Double.valueOf(range[1])/100;
        } else {
            startHour = Double.valueOf(startRange[0]);
        }

        if (endRange[0].contains(":")) {
            String[] range = endRange[0].split(":");
            endHour = Double.valueOf(range[0]);
            endMinute = Double.valueOf(range[1])/100;
        } else {
            endHour = Double.valueOf(endRange[0]);
        }

        if (start.substring(start.length() - 2).equals("pm") && !start.substring(0, 2).equals("12"))
            startHour += 12;

        else if (start.substring(start.length() - 2).equals("am") && start.substring(0, 2).equals("12")) //Make 12am to 0
            startHour = 0;

        if (end.substring(end.length() - 2).equals("pm"))
            endHour += 12;

        else if (end.substring(end.length() - 2).equals("am") && end.substring(0, 2).equals("12")) //Make 12am to 24
            endHour = 24;

        else if (end.substring(end.length() - 2).equals("am")){
            endHour = 24 + Integer.valueOf(end.substring(0, 1));
        }

        double startTime = startHour + startMinute;
        double endTime = endHour + endMinute;

        double[] timeRange = {startTime, endTime};

        return timeRange;
    }

    public double getTimeNow(){
        Calendar c = Calendar.getInstance();
        double hourNow = Double.valueOf(c.get(Calendar.HOUR_OF_DAY));
        double minute = Double.valueOf(c.get(Calendar.MINUTE))/100;

        if (hourNow == 0){
            hourNow = 24;
        }

        else if (hourNow == 1){
            hourNow = 25;
        }

        else if (hourNow == 2){
            hourNow = 26;
        }

        else if (hourNow == 3){
            hourNow = 27;
        }

        return hourNow + minute;
    }

    public String getTodaysTime(String[] preprocess){
        String time = null;
        for (String times : preprocess){
            if (times.equals(""))
                continue;
            String[] timeArray = times.split(":", 2);
            time = timeArray[1].trim();
            String day = timeArray[0].trim();
            if (isDay(day)){
                return time;
            }
        }
        return time;
    }

    private boolean isDay(String day) {
        String[] splitDay = day.split("&");
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_WEEK);
        boolean isToday = false;
        for (String days : splitDay) {

            if (days.contains("-")){
                String[] dayRange = days.split("-");
                int startDay = getDay(dayRange[0].trim());
                int endDay = getDay(dayRange[1].trim());
                if (startDay <= today && today <= endDay){
                    isToday = true;
                    break;
                }

            }
            else{
                int dayInt = getDay(days.trim());
                if (today == dayInt){
                    isToday = true;
                    break;
                }
            }
        }

        return isToday;
    }

    private int getDay(String day) {
        if (day.equals("Sun"))
            return 1;

        if (day.equals("Mon"))
            return 2;

        if (day.equals("Tue"))
            return 3;

        if (day.equals("Wed"))
            return 4;

        if (day.equals("Thurs"))
            return 5;

        if (day.equals("Fri"))
            return 6;

        if (day.equals("Sat"))
            return 7;

        if (day.equals("Everyday"))
            return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        return -1;
    }

}
