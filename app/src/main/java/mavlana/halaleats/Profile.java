package mavlana.halaleats;

import android.app.Activity;

/**
 * Created by samir on 22/04/16.
 */
public class Profile {
    private Activity activity = null;
    private static final int PROFILE_PIC_SIZE = 400;
    private String name;
    private String userID;
    private String personPhotoUrl;

    public Profile(Activity activity){
        this.activity = activity;
    }

    public void getProfileInformation(){
        name = activity.getIntent().getStringExtra("Name");
        userID = "G" + activity.getIntent().getStringExtra("ID");
        personPhotoUrl = activity.getIntent().getStringExtra("Image");

        // by default the profile url gives 50x50 px image only
        // we can replace the value with whatever dimension we want by
        // replacing sz=X
        personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 2)
                + PROFILE_PIC_SIZE;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setUserID(String userID){
        this.userID = userID;
    }

    public String getUserID(){
        return this.userID;
    }

    public void setPersonPhotoUrl(String personPhotoUrl){
        this.personPhotoUrl = personPhotoUrl;
    }

    public String getPersonPhotoUrl(){
        return this.personPhotoUrl;
    }

}
