package mavlana.halaleats;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ProfilePage extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private ArrayList<RestaurantInfo> listOfRestaurants;
    private List<RestaurantInfo> foundNew;
    private String mSearchQuery = "";
    private static final String TAG = "Profile Picture";
    private GoogleApiClient mGoogleApiClient;
    private TextView test;
    private static final int PROFILE_PIC_SIZE = 400;
    private ImageView imgProfilePic;
    private String loginType;
    private String userID;
    private String name;
    private Bitmap a;
    private String personPhotoUrl;
    private AutoCompleteTextView searchBar;
    private ImageButton filter;
    private RadioGroup searchRadio;
    private ListView lv;
    private ArrayAdapter<RestaurantInfo> arrayAdapter;
    private ArrayAdapter<RestaurantInfo> favouritesAdapter;
    private ArrayAdapter<String> nameAdapter;
    private double lat = 0;
    private double lng = 0;
    private boolean listLoaded;
    private boolean firstLoad = true;
    private TreeSet<String> restaurantsName;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private List<RestaurantInfo> listOfRestaurantsFiltered = new ArrayList<>();
    private boolean pressed = false;
    private List<Item> items;
    private Header citiesHeader = new Header("Location");
    private Header cuisineHeader = new Header("Cuisine");
    private Header priceHeader = new Header("Price Range");
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyC8qKk2TxybZIMmaQVbo9SqKBOlByBmmpI";
    private List<String> rIDs;
    private ArrayList<RestaurantInfo> favourites;
    private ListView favouritesList;
    private boolean favouritesLoaded = false;
    private boolean profileView = false;
    private TwoTextArrayAdapter tt;
    private boolean activityStarted = false;
    private String fbID;
    private TreeSet<String> cities = new TreeSet<>();
    private TreeSet<String> cuisines = new TreeSet<>();
    private TreeSet<String> prices = new TreeSet<>();
    private float[] resultArray = new float[99];
    private String[] cuisineArray;

    LocationManager manager;
    boolean statusOfGPS;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected String mLastUpdateTime;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        loginType = getIntent().getStringExtra("Login Type");
        test = (TextView) findViewById(R.id.test);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);

        if (loginType.equals("Google")) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addApi(LocationServices.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .build();
            createLocationRequest();
            mGoogleApiClient.connect();
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLastUpdateTime = "";

        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
            mGoogleApiClient.connect();
            manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLastUpdateTime = "";

            fbID = getIntent().getStringExtra("ID");
            userID = "FB" + fbID;
            getProfileView();
            name = getIntent().getStringExtra("Name");
            test.setText(name);
        }

        statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!statusOfGPS) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            //Setting Dialog Title
            alertDialog.setTitle("GPS Disabled");

            //Setting Dialog Message
            alertDialog.setMessage("To find the restaurants nearest to you, please turn on your GPS");

            //On Pressing Setting button
            alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    ProfilePage.this.startActivity(intent);
                }
            });

            //On pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }

    }

    public Bitmap getUserPic(String userID) {
        String imageURL;
        Bitmap bitmap = null;
        Log.d(TAG, "Loading Picture");
        imageURL = "https://graph.facebook.com/" + userID + "/picture?type=large";
        new LoadProfileImage(imgProfilePic).execute(imageURL);

        return bitmap;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_page, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (loginType.equals("Google")) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!statusOfGPS) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                //Setting Dialog Title
                alertDialog.setTitle("GPS Disabled");

                //Setting Dialog Message
                alertDialog.setMessage("To find the restaurants nearest to you, please turn on your GPS");

                //On Pressing Setting button
                alertDialog.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        ProfilePage.this.startActivity(intent);
                    }
                });

                //On pressing cancel button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
            }
//            startLocationUpdates();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (lat == 0 && lng == 0) {
            startLocationUpdates();
        }
        if (loginType.equals("Google")) {
            getProfileInformation();
        }
        if (mCurrentLocation == null && listLoaded) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            if (mCurrentLocation != null) {
                lat = mCurrentLocation.getLatitude();
                lng = mCurrentLocation.getLongitude();
                for (RestaurantInfo r : listOfRestaurants) {
                    r.updateDistance(lat, lng);
                }
                Collections.sort(listOfRestaurants);
                if (!(foundNew == null)) {
                    for (RestaurantInfo r : foundNew) {
                        r.updateDistance(lat, lng);
                    }
                    Collections.sort(foundNew);
                }

                arrayAdapter.notifyDataSetChanged();
            }
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void getProfileInformation() {
        try {

            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                name = currentPerson.getDisplayName();
                userID = "G" + currentPerson.getId();
                personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                Log.e(TAG, "Name: " + name + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + PROFILE_PIC_SIZE;


                getProfileView();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Background Async task to load user profile picture from url
     */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            ImageHelper n = new ImageHelper();
            Bitmap rounded = n.getRoundedCornerBitmap(mIcon11, 400);
            return rounded;
        }

        protected void onPostExecute(Bitmap result) {
            a = result;
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchBar != null && searchBar.hasFocus()) {
            searchBar.clearFocus();
        }

    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.btnProfile:
                if (checked)
                    getProfileView();
                break;
            case R.id.btnSearch:
                if (checked)
                    getSearchView();
                break;
        }
    }

    public void getProfileView() {
        setContentView(R.layout.activity_profile_page);

//        if (lat == 0 && lng == 0) {
//            startLocationUpdates();
//        }
        profileView = true;
        favouritesLoaded = false;
        test = (TextView) findViewById(R.id.test);
        favouritesList = (ListView) findViewById(R.id.favourites);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        if (loginType.equals("Google")) {
            new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
        } else {
            a = getUserPic(fbID);
            imgProfilePic.setImageBitmap(a);
        }
        favouritesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RestaurantInfo r = (RestaurantInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent(ProfilePage.this, RestaurantsInfo.class);
                intent.putExtra("rID", r.getrID());
                intent.putExtra("Name", r.getName());
                intent.putExtra("Address", r.getAddress());
                intent.putExtra("Cuisine", r.cuisineString());
                intent.putExtra("Latitude", String.valueOf(r.getLat()));
                intent.putExtra("Longitude", String.valueOf(r.getLng()));
                intent.putExtra("Number", r.getPhoneNumber());
                intent.putExtra("Web", r.getWebsite());
                intent.putExtra("Hours", r.timeToString());
                intent.putExtra("MyLatitude", String.valueOf(lat));
                intent.putExtra("MyLongitude", String.valueOf(lng));
                intent.putExtra("ID", userID);
                intent.putExtra("Location", r.getCity());
                intent.putExtra("Price", r.getPrice());
                r.setFavourite(false);
                for (RestaurantInfo a : favourites) {
                    if (a.getrID().equals(r.getrID())) {
                        r.setFavourite(true);
                        break;
                    }
                }
                intent.putExtra("Favourite", r.getFavourite());
                activityStarted = true;
                startActivity(intent);
            }
        });

        test.setText(name);
        favourites = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(userID);
        rIDs = new ArrayList<>();
        query.whereExists("rID");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    count = 1;
                    for (ParseObject post : objects) {
                        String[] info = new String[11];
                        info[0] = post.getString("rID");
                        info[1] = post.getString("RestaurantName");
                        info[2] = post.getString("Address");
                        info[3] = post.getString("PhoneNumber");
                        info[4] = post.getString("Latitude");
                        info[5] = post.getString("Longitude");
                        info[6] = post.getString("Website");
                        info[7] = post.getString("Location");
                        info[8] = post.getString("Cuisine");
                        info[9] = post.getString("Price");
                        info[10] = post.getString("Time");

                        RestaurantInfo r = new RestaurantInfo(info);
                        r.setFavourite(true);
                        r.updateDistance(lat, lng);
                        favourites.add(r);

                        if (objects.size() == count){
                            favouritesLoaded = true;
                            Collections.sort(favourites);
                            if (profileView) {
                                favouritesAdapter = new ArrayAdapter<>(ProfilePage.this, android.R.layout.simple_list_item_1,
                                        favourites);
                                favouritesList.setAdapter(favouritesAdapter);
                                favouritesAdapter.notifyDataSetChanged();
                            }
                        } else {
                            count++;
                        }
                    }
                }
            }

        });

    }

    public void getSearchView() {
        setContentView(R.layout.activity_search);
        profileView = false;
//        if (lat == 0 && lng == 0) {
//            startLocationUpdates();
//        }

        if (listLoaded){
            lv = (ListView) findViewById(R.id.list_view);
            searchBar = (AutoCompleteTextView) findViewById(R.id.search);
            searchBar.addTextChangedListener(new SearchWatcher());
            searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        searchRadio.setVisibility(View.INVISIBLE);
                    } else {
                        searchRadio.setVisibility(View.VISIBLE);
                    }
                }
            });
            mDrawerLayout = (DrawerLayout) findViewById(R.id.profile_page);
            //filter = (ImageButton) findViewById(R.id.filter);
            searchRadio = (RadioGroup) findViewById(R.id.searchRadio);
            searchRadio.setVisibility(View.INVISIBLE);
            lv = (ListView) findViewById(R.id.list_view);

            mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    searchBar.clearFocus();

                }

                @Override
                public void onDrawerOpened(View drawerView) {

                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    if (pressed) {
                        pressed = false;
                        restaurantsName = new TreeSet<>();
                        foundNew = new ArrayList<>();
                        Iterator<Item> it = items.iterator();
                        Item a;
                        it.next();
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                for (Iterator<RestaurantInfo> r = listOfRestaurants.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getCity().equals(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty()) {
                            foundNew = new ArrayList<RestaurantInfo>(listOfRestaurants);
                        }

                        List<RestaurantInfo> foundOld = new ArrayList<>(foundNew);
                        foundNew = new ArrayList<>();
                        int count2 = 0;
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                count2++;
                                for (Iterator<RestaurantInfo> r = foundOld.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getCuisine().contains(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty() && count2 == 0) {
                            foundNew = new ArrayList<>(foundOld);
                        }

                        foundOld = new ArrayList<>(foundNew);
                        foundNew = new ArrayList<>();
                        count2 = 0;
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                count2++;
                                for (Iterator<RestaurantInfo> r = foundOld.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getPrice().equals(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty() && count2 == 0) {
                            foundNew = new ArrayList<RestaurantInfo>(foundOld);
                        }

                        Collections.sort(foundNew);
                        for (RestaurantInfo r : foundNew) {
                            restaurantsName.add(r.getName());
                        }
                        ArrayList<String> temp = new ArrayList<>(restaurantsName);
                        nameAdapter = new ArrayAdapter<>(ProfilePage.this,
                                android.R.layout.simple_list_item_1,
                                temp);
                        searchBar.setAdapter(nameAdapter);
                        arrayAdapter = new ArrayAdapter<>(ProfilePage.this,
                                android.R.layout.simple_list_item_1,
                                foundNew);
                        lv.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            Collections.sort(listOfRestaurants);
            arrayAdapter = new ArrayAdapter<RestaurantInfo>(ProfilePage.this,
                    android.R.layout.simple_list_item_1,
                    listOfRestaurants);
            ArrayList<String> temp = new ArrayList<>(restaurantsName);
            nameAdapter = new ArrayAdapter<String>(ProfilePage.this,
                    android.R.layout.simple_list_item_1,
                    temp);

            lv.setAdapter(arrayAdapter);
            searchBar.setAdapter(nameAdapter);
            listLoaded = true;
            listOfRestaurantsFiltered = listOfRestaurants;
            items = new ArrayList<>();
            items.add(citiesHeader);
            ;
            for (RestaurantInfo r : listOfRestaurants) {
                cities.add(r.getCity());
            }
            for (String city : cities) {
                items.add(new ListItem(city, "city"));
            }
            items.add(cuisineHeader);

            for (String cuisine : cuisines) {
                items.add(new ListItem(cuisine, "cuisine"));
            }
            items.add(priceHeader);
            for (String price : prices) {
                items.add(new ListItem(price, "price"));
            }
            tt = new TwoTextArrayAdapter(ProfilePage.this, items);
            mDrawerList.setAdapter(tt);
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RestaurantInfo r = (RestaurantInfo) parent.getItemAtPosition(position);
                    Intent intent = new Intent(ProfilePage.this, RestaurantsInfo.class);
                    intent.putExtra("rID", r.getrID());
                    intent.putExtra("Name", r.getName());
                    intent.putExtra("Address", r.getAddress());
                    intent.putExtra("Cuisine", r.cuisineString());
                    intent.putExtra("Latitude", String.valueOf(r.getLat()));
                    intent.putExtra("Longitude", String.valueOf(r.getLng()));
                    intent.putExtra("Number", r.getPhoneNumber());
                    intent.putExtra("Web", r.getWebsite());
                    intent.putExtra("Hours", r.timeToString());
                    intent.putExtra("MyLatitude", String.valueOf(lat));
                    intent.putExtra("MyLongitude", String.valueOf(lng));
                    intent.putExtra("ID", userID);
                    intent.putExtra("Location", r.getCity());
                    intent.putExtra("Price", r.getPrice());
                    r.setFavourite(false);
                    for (RestaurantInfo a : favourites){
                        if (a.getrID().equals(r.getrID())){
                            r.setFavourite(true);
                            break;
                        }
                    }
                    intent.putExtra("Favourite", r.getFavourite());
                    activityStarted = true;
                    startActivity(intent);
                }
            });
        }
        else {
            listLoaded = false;

            searchBar = (AutoCompleteTextView) findViewById(R.id.search);
            searchBar.addTextChangedListener(new SearchWatcher());
            searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        searchRadio.setVisibility(View.INVISIBLE);
                    } else {
                        searchRadio.setVisibility(View.VISIBLE);
                    }
                }
            });
            mDrawerLayout = (DrawerLayout) findViewById(R.id.profile_page);
            //filter = (ImageButton) findViewById(R.id.filter);
            searchRadio = (RadioGroup) findViewById(R.id.searchRadio);
            searchRadio.setVisibility(View.INVISIBLE);
            lv = (ListView) findViewById(R.id.list_view);
            RestaurantInfoArray restaurants = new RestaurantInfoArray();
            listOfRestaurants = new ArrayList<>();
            restaurantsName = new TreeSet<>();


            mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    searchBar.clearFocus();

                }

                @Override
                public void onDrawerOpened(View drawerView) {

                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    if (pressed) {
                        pressed = false;
                        restaurantsName = new TreeSet<>();
                        foundNew = new ArrayList<>();
                        Iterator<Item> it = items.iterator();
                        Item a;
                        it.next();
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                for (Iterator<RestaurantInfo> r = listOfRestaurants.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getCity().equals(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty()) {
                            foundNew = new ArrayList<RestaurantInfo>(listOfRestaurants);
                        }

                        List<RestaurantInfo> foundOld = new ArrayList<>(foundNew);
                        foundNew = new ArrayList<>();
                        int count2 = 0;
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                count2++;
                                for (Iterator<RestaurantInfo> r = foundOld.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getCuisine().contains(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty() && count2 == 0) {
                            foundNew = new ArrayList<>(foundOld);
                        }

                        foundOld = new ArrayList<>(foundNew);
                        foundNew = new ArrayList<>();
                        count2 = 0;
                        while (it.hasNext()) {
                            a = it.next();
                            if (a.getName2().equals("null")) {
                                break;
                            }
                            if (a.isClicked()) {
                                count2++;
                                for (Iterator<RestaurantInfo> r = foundOld.iterator(); r.hasNext(); ) {
                                    RestaurantInfo current = r.next();
                                    if (current.getPrice().equals(a.getName())) {
                                        foundNew.add(current);
                                    }
                                }
                            }

                        }
                        if (foundNew.isEmpty() && count2 == 0) {
                            foundNew = new ArrayList<RestaurantInfo>(foundOld);
                        }

                        Collections.sort(foundNew);
                        for (RestaurantInfo r : foundNew) {
                            restaurantsName.add(r.getName());
                        }
                        ArrayList<String> temp = new ArrayList<>(restaurantsName);
                        nameAdapter = new ArrayAdapter<>(ProfilePage.this,
                                android.R.layout.simple_list_item_1,
                                temp);
                        searchBar.setAdapter(nameAdapter);
                        arrayAdapter = new ArrayAdapter<>(ProfilePage.this,
                                android.R.layout.simple_list_item_1,
                                foundNew);
                        lv.setAdapter(arrayAdapter);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onDrawerStateChanged(int newState) {

                }
            });
            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            if (favourites == null) {
                favourites = new ArrayList<>();
                ParseQuery<ParseObject> query = ParseQuery.getQuery(userID);
                rIDs = new ArrayList<>();
                query.whereExists("Restaurants");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            for (ParseObject post : objects) {
                                rIDs.add(post.getString("Restaurants"));
                            }
                        }

                        for (final String rID : rIDs) {
                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Restaurants");
                            query2.whereEqualTo("rID", rID);
                            query2.getFirstInBackground(new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException e) {
                                    if (object != null) {
                                        String[] info = new String[11];
                                        info[0] = object.getString("rID");
                                        info[1] = object.getString("RestaurantName");
                                        info[2] = object.getString("Address");
                                        info[3] = object.getString("PhoneNumber");
                                        info[4] = object.getString("Latitude");
                                        info[5] = object.getString("Longitude");
                                        info[6] = object.getString("Website");
                                        info[7] = object.getString("Location");
                                        info[8] = object.getString("Cuisine");
                                        info[9] = object.getString("Price");
                                        info[10] = object.getString("Time");

                                        RestaurantInfo r = new RestaurantInfo(info);
                                        r.setFavourite(true);
                                        r.updateDistance(lat, lng);
                                        favourites.add(r);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private class RestaurantInfoArray{

        public RestaurantInfoArray(){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Restaurants");
            query.setLimit(1000);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> object, ParseException e) {
                    for (ParseObject post : object) {
                        String[] info = new String[11];
                        info[0] = post.getString("rID");
                        info[1] = post.getString("RestaurantName");
                        info[2] = post.getString("Address");
                        info[3] = post.getString("PhoneNumber");
                        info[4] = post.getString("Latitude");
                        info[5] = post.getString("Longitude");
                        info[6] = post.getString("Website");
                        info[7] = post.getString("Location");
                        info[8] = post.getString("Cuisine");
                        info[9] = post.getString("Price");
                        info[10] = post.getString("Time");

                        RestaurantInfo r = new RestaurantInfo(info);
                        cities.add(r.getCity());
                        cuisineArray = r.getCuisine().split("\\+");
                        for (String a : cuisineArray) {
                            cuisines.add(a);
                        }
                        prices.add(r.getPrice());
                        if (r.getLat() != 0) {
                            Location.distanceBetween(lat, lng, r.getLat(), r.getLng(), resultArray);

                            r.setDistance(new DecimalFormat("##.#").format(resultArray[0] / 1000));
                        }
                        listOfRestaurants.add(r);
                        restaurantsName.add(r.getName());
                    }
                    Collections.sort(listOfRestaurants);
                    arrayAdapter = new ArrayAdapter<RestaurantInfo>(ProfilePage.this,
                            android.R.layout.simple_list_item_1,
                            listOfRestaurants);
                    ArrayList<String> temp = new ArrayList<>(restaurantsName);
                    nameAdapter = new ArrayAdapter<String>(ProfilePage.this,
                            android.R.layout.simple_list_item_1,
                            temp);

                    lv.setAdapter(arrayAdapter);
                    searchBar.setAdapter(nameAdapter);
                    listLoaded = true;
                    listOfRestaurantsFiltered = listOfRestaurants;
                    items = new ArrayList<>();
                    items.add(citiesHeader);
                    ;
                    for (RestaurantInfo r : listOfRestaurants) {
                        cities.add(r.getCity());
                    }
                    for (String city : cities) {
                        items.add(new ListItem(city, "city"));
                    }
                    items.add(cuisineHeader);

                    for (String cuisine : cuisines) {
                        items.add(new ListItem(cuisine, "cuisine"));
                    }
                    items.add(priceHeader);
                    for (String price : prices) {
                        items.add(new ListItem(price, "price"));
                    }
                    tt = new TwoTextArrayAdapter(ProfilePage.this, items);
                    mDrawerList.setAdapter(tt);
                    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            RestaurantInfo r = (RestaurantInfo) parent.getItemAtPosition(position);
                            Intent intent = new Intent(ProfilePage.this, RestaurantsInfo.class);
                            intent.putExtra("rID", r.getrID());
                            intent.putExtra("Name", r.getName());
                            intent.putExtra("Address", r.getAddress());
                            intent.putExtra("Cuisine", r.cuisineString());
                            intent.putExtra("Latitude", String.valueOf(r.getLat()));
                            intent.putExtra("Longitude", String.valueOf(r.getLng()));
                            intent.putExtra("Number", r.getPhoneNumber());
                            intent.putExtra("Web", r.getWebsite());
                            intent.putExtra("Hours", r.timeToString());
                            intent.putExtra("MyLatitude", String.valueOf(lat));
                            intent.putExtra("MyLongitude", String.valueOf(lng));
                            intent.putExtra("ID", userID);
                            intent.putExtra("Location", r.getCity());
                            intent.putExtra("Price", r.getPrice());
                            r.setFavourite(false);
                            for (RestaurantInfo a : favourites) {
                                if (a.getrID().equals(r.getrID())) {
                                    r.setFavourite(true);
                                    break;
                                }
                            }
                            intent.putExtra("Favourite", r.getFavourite());
                            activityStarted = true;
                            startActivity(intent);
                        }
                    });
                }


            });

            if (listOfRestaurants != null) {
                Collections.sort(listOfRestaurants);
            }

        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (listLoaded && mGoogleApiClient.isConnected()) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            lat = location.getLatitude();
            lng = location.getLongitude();
            for (RestaurantInfo r : listOfRestaurants) {
                r.updateDistance(lat, lng);
            }

            Collections.sort(listOfRestaurants);
            if (!(foundNew == null)) {
                for (RestaurantInfo r : foundNew) {
                    r.updateDistance(lat, lng);
                }
                Collections.sort(foundNew);
            }

            arrayAdapter.notifyDataSetChanged();
            stopLocationUpdates();

        }

        else if (profileView && favouritesLoaded && mGoogleApiClient.isConnected()){
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            lat = location.getLatitude();
            lng = location.getLongitude();

            for (RestaurantInfo r : favourites){
                r.updateDistance(lat, lng);
            }
            Collections.sort(favourites);
            favouritesAdapter.notifyDataSetChanged();
            stopLocationUpdates();
        }


    }

    public void onSearchButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.restaurants:
                if (checked) {
                    ArrayList<String> temp = new ArrayList<>(restaurantsName);
                    nameAdapter = new ArrayAdapter<>(ProfilePage.this,
                            android.R.layout.simple_list_item_1,
                            temp);
                    searchBar = (AutoCompleteTextView) findViewById(R.id.search);
                    searchBar.addTextChangedListener(new SearchWatcher());
                    searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (!b) {
                                searchRadio.setVisibility(View.INVISIBLE);
                            } else {
                                searchRadio.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    searchBar.setAdapter(nameAdapter);
                    searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            searchBar.clearFocus();
                            view = ProfilePage.this.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    });
                }
                break;
            case R.id.address:
                if (checked) {
                    searchBar = (AutoCompleteTextView) findViewById(R.id.search);
                    searchBar.addTextChangedListener(new SearchWatcher());
                    searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean b) {
                            if (!b) {
                                searchRadio.setVisibility(View.INVISIBLE);
                            } else {
                                searchRadio.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    searchBar.setAdapter(new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1));
                    searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String str = (String) parent.getItemAtPosition(position);
                            System.out.println("Address Selected: " + str);
                            final Geocoder coder = new Geocoder(ProfilePage.this);
                            final List<Address>[] addressArray = new ArrayList[1];
                            addressArray[0] = new ArrayList<>();
                            final String[] address = new String[1];
                            try {
                                address[0] = str;
                                try {
                                    addressArray[0] = coder.getFromLocationName(address[0], 5, 43.5742334, -80.530535, 45.400196, -75.696550);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                final List<Address> finalAddressArray = addressArray[0];

                                lat = finalAddressArray.get(0).getLatitude();
                                lng = finalAddressArray.get(0).getLongitude();

                                for (RestaurantInfo r : listOfRestaurants) {
                                    r.updateDistance(lat, lng);

                                }
                                if (!(foundNew == null)) {
                                    for (RestaurantInfo r : foundNew) {
                                        r.updateDistance(lat, lng);
                                    }
                                    Collections.sort(foundNew);
                                }
                                Collections.sort(listOfRestaurants);

                                arrayAdapter.notifyDataSetChanged();
                                searchBar.setText("");
                                searchBar.clearFocus();
                                view = ProfilePage.this.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                final RadioButton radio1 = (RadioButton) findViewById(R.id.restaurants);
                                radio1.setChecked(true);

                            }
                            catch (Exception e){

                            }

                        }
                    });

                }
                break;
        }
    }

    private class SearchWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            mSearchQuery = searchBar.getText().toString();
            if (!(foundNew == null)) {
                listOfRestaurantsFiltered = performSearch(foundNew, mSearchQuery);
            } else {
                listOfRestaurantsFiltered = performSearch(listOfRestaurants, mSearchQuery);
            }
            arrayAdapter = new ArrayAdapter<RestaurantInfo>(ProfilePage.this,
                    android.R.layout.simple_list_item_1,
                    listOfRestaurantsFiltered);
            lv.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
        }

    }

    /**
     * Goes through the given list and filters it according to the given query.
     *
     * @param movies list given as search sample
     * @param query  to be searched
     * @return new filtered list
     */
    private List<RestaurantInfo> performSearch(List<RestaurantInfo> movies, String query) {

        // First we split the query so that we're able
        // to search word by word (in lower case).
        String queryByWords = query.toLowerCase();

        // Empty list to fill with matches.
        List<RestaurantInfo> moviesFiltered = new ArrayList<RestaurantInfo>();

        // Go through initial releases and perform search.
        for (RestaurantInfo movie : movies) {

            // Content to search through (in lower case).
            String content = (
                    movie.getName()
            ).toLowerCase();

            // All query words have to be contained,
            // otherwise the release is filtered out.
            if (content.contains(queryByWords)) {
                moviesFiltered.add(movie);


            }

        }

        return moviesFiltered;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            try {
                ListItem city = (ListItem) mDrawerList.getItemAtPosition(position);
                if (city.isClicked()) {
                    view.setActivated(false);
                    ;
                    //view.setBackgroundResource(R.color.black);
                    city.toggleClicked();
                } else {
                    //view.setBackgroundResource(R.color.green);
                    view.setActivated(true);
                    city.toggleClicked();
                }
                pressed = true;
            } catch (Exception e) {
            }

        }
    }

    @Override
    public void onRestart(){
        if (activityStarted){
            count = 1;
            activityStarted = false;
                favourites = new ArrayList<>();
                ParseQuery<ParseObject> query = ParseQuery.getQuery(userID);
                rIDs = new ArrayList<>();
                query.whereExists("rID");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            count = 1;
                            for (ParseObject post : objects) {
                                String[] info = new String[11];
                                info[0] = post.getString("rID");
                                info[1] = post.getString("RestaurantName");
                                info[2] = post.getString("Address");
                                info[3] = post.getString("PhoneNumber");
                                info[4] = post.getString("Latitude");
                                info[5] = post.getString("Longitude");
                                info[6] = post.getString("Website");
                                info[7] = post.getString("Location");
                                info[8] = post.getString("Cuisine");
                                info[9] = post.getString("Price");
                                info[10] = post.getString("Time");

                                RestaurantInfo r = new RestaurantInfo(info);
                                r.setFavourite(true);
                                r.updateDistance(lat, lng);
                                favourites.add(r);

                                if (objects.size() == count) {
                                    favouritesLoaded = true;
                                    Collections.sort(favourites);
                                    if (profileView) {
                                        favouritesAdapter = new ArrayAdapter<>(ProfilePage.this, android.R.layout.simple_list_item_1,
                                                favourites);
                                        favouritesList.setAdapter(favouritesAdapter);
                                        favouritesAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    count++;
                                }
                            }
                        }
                    }
                });
        }
        super.onRestart();
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:ca");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }
}



