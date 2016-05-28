package mavlana.halaleats;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ProfilePage extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://flickering-inferno-2585.firebaseio.com/");
    private ArrayList<RestaurantInfo> listOfRestaurants;
    private static final int LOCATION_PERMISSIONS = 21;
    private List<RestaurantInfo> foundNew;
    private static final String TAG = "Profile Picture";
    private GoogleApiClient mGoogleApiClient;
    private TextView test;
    private ImageView imgProfilePic;
    private String loginType;
    private String userID;
    private String name;
    private String personPhotoUrl;
    private AutoCompleteTextView searchBar;
    private RadioGroup searchRadio;
    private ListView lv;
    private CustomListAdapter arrayAdapter;
    private CustomListAdapter favouritesAdapter;
    private ArrayAdapter<String> nameAdapter;
    private double lat = 0;
    private double lng = 0;
    private boolean listLoaded;
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
    private boolean mRequestingLocationUpdates = false;

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

            Profile profile = new Profile(this);
            name = profile.getName();
            userID = profile.getUserID();
            System.out.println(userID);
            personPhotoUrl = profile.getPersonPhotoUrl();

        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            fbID = getIntent().getStringExtra("ID");
            userID = "FB" + fbID;
            name = getIntent().getStringExtra("Name");
            test.setText(name);
        }

        createLocationRequest();
        mGoogleApiClient.connect();
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLastUpdateTime = "";
        getProfileView();

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
        if (id == R.id.start_location) {
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
            startLocationUpdates();
        } else if (id == R.id.stop_location) {
            stopLocationUpdates();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {

        if ((lat == 0 && lng == 0) || (mCurrentLocation == null && listLoaded)) {
            startLocationUpdates();
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

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
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

        profileView = true;
        favouritesLoaded = false;
        test = (TextView) findViewById(R.id.test);
        favouritesList = (ListView) findViewById(R.id.favourites);
        imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
        if (loginType.equals("Google")) {
            new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);
        } else {
            String imageURL;
            Log.d(TAG, "Loading Picture");
            imageURL = "https://graph.facebook.com/" + fbID + "/picture?type=large";
            new LoadProfileImage(imgProfilePic).execute(imageURL);
        }
        getInfoPage(favouritesList);

        test.setText(name);
        favourites = new ArrayList<>();
        getFavouritesArray();


    }

    public void getSearchView() {
        setContentView(R.layout.activity_search);
        profileView = false;

        if (listLoaded) {
            lv = (ListView) findViewById(R.id.list_view);
            searchBar = (AutoCompleteTextView) findViewById(R.id.search);
            searchBar.addTextChangedListener(new SearchWatcher());
            searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        searchRadio.setVisibility(View.INVISIBLE);
                        searchBar.clearFocus();
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
            setDrawerLayout();

            mDrawerList = (ListView) findViewById(R.id.left_drawer);

            Collections.sort(listOfRestaurants);
            arrayAdapter = new CustomListAdapter(ProfilePage.this,
                    R.layout.custom_list,
                    listOfRestaurants, "gothic_0.TTF");
            ArrayList<String> temp = new ArrayList<>(restaurantsName);
            nameAdapter = new ArrayAdapter<String>(ProfilePage.this,
                    android.R.layout.simple_list_item_1,
                    temp);

            getRestaurantAdapter();

            getInfoPage(lv);

        } else {
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
            listOfRestaurants = new ArrayList<>();
            restaurantsName = new TreeSet<>();
            favourites = new ArrayList<>();

            setDrawerLayout();

            mDrawerList = (ListView) findViewById(R.id.left_drawer);
            //Find list of restaurants
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> restaurantsIterator = dataSnapshot.getChildren().iterator();
                    DataSnapshot restaurants = null;
                    while (restaurantsIterator.hasNext()){
                        restaurants = restaurantsIterator.next();
                        if (restaurants.getKey().equals("results"))
                            break;
                    }
                    for (DataSnapshot restaurantInfo : restaurants.getChildren()){
                        String[] info = getRestaurantInfo(restaurantInfo);
                        RestaurantInfo r = new RestaurantInfo(info);
                        cities.add(r.getCity());
                        cuisineArray = r.getCuisine().split("\\+");
                        for (String a : cuisineArray) {
                            cuisines.add(a);
                        }
                        prices.add(r.getPrice());
                        if (r.getLatitude() != 0) {
                            Location.distanceBetween(lat, lng, r.getLatitude(), r.getLongitude(), resultArray);

                            r.setDistance(new DecimalFormat("##.#").format(resultArray[0] / 1000));
                        }
                        listOfRestaurants.add(r);
                        restaurantsName.add(r.getName());
                    }

                    Collections.sort(listOfRestaurants);
                    arrayAdapter = new CustomListAdapter(ProfilePage.this,
                            R.layout.custom_list, listOfRestaurants, "gothic_0.TTF");
                    ArrayList<String> temp = new ArrayList<>(restaurantsName);
                    nameAdapter = new ArrayAdapter<String>(ProfilePage.this,
                            android.R.layout.simple_list_item_1,
                            temp);

                    getRestaurantAdapter();
                    getInfoPage(lv);

                    listLoaded = true;
                    listOfRestaurantsFiltered = listOfRestaurants;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });

        }
    }

    private void getRestaurantAdapter() {
        lv.setAdapter(arrayAdapter);
        searchBar.setAdapter(nameAdapter);
        listOfRestaurantsFiltered = listOfRestaurants;
        items = new ArrayList<>();
        items.add(new ListItem("Open Now", "Status"));
        items.add(citiesHeader);

        for (RestaurantInfo r : listOfRestaurants) {
            cities.add(r.getCity());
        }
        for (String city : cities) {
            items.add(new ListItem(city, "City"));
        }
        items.add(cuisineHeader);

        for (String cuisine : cuisines) {
            items.add(new ListItem(cuisine, "Cuisine"));
        }
        items.add(priceHeader);
        for (String price : prices) {
            items.add(new ListItem(price, "Price"));
        }
        tt = new TwoTextArrayAdapter(ProfilePage.this, items);
        mDrawerList.setAdapter(tt);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setDrawerLayout() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
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
                    boolean openQuery = it.next().isClicked();
                    System.out.println(openQuery);
                    it.next();
                    while (it.hasNext()) {
                        a = it.next();
                        if (a.getName2().equals("null")) {
                            break;
                        }
                        if (a.isClicked()) {
                            for (RestaurantInfo current : listOfRestaurants) {
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

                    foundOld = new ArrayList<>(foundNew);
                    foundNew = new ArrayList<>();

                    if (openQuery) {
                        for (Iterator<RestaurantInfo> r = foundOld.iterator(); r.hasNext(); ) {
                            RestaurantInfo current = r.next();
                            if (current.isOpen()) {
                                foundNew.add(current);
                            }
                        }
                    }

                    if (foundNew.isEmpty() && count2 == 0) {
                        foundNew = new ArrayList<RestaurantInfo>(foundOld);
                    }
                    Collections.sort(foundNew);

                    ArrayList<String> temp = new ArrayList<>(restaurantsName);
                    nameAdapter = new ArrayAdapter<>(ProfilePage.this,
                            android.R.layout.simple_list_item_1,
                            temp);
                    searchBar.setAdapter(nameAdapter);
                    arrayAdapter = new CustomListAdapter(ProfilePage.this,
                            R.layout.custom_list,
                            foundNew, "gothic_0.TTF");
                    lv.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
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
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                                System.out.println(listOfRestaurants.size());
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
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                final RadioButton radio1 = (RadioButton) findViewById(R.id.restaurants);
                                radio1.setChecked(true);

                            } catch (Exception e) {
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
            String mSearchQuery = searchBar.getText().toString();
            if (!(foundNew == null)) {
                listOfRestaurantsFiltered = performSearch(foundNew, mSearchQuery);
            } else {
                listOfRestaurantsFiltered = performSearch(listOfRestaurants, mSearchQuery);
            }
            arrayAdapter = new CustomListAdapter(ProfilePage.this,
                    R.layout.custom_list,
                    listOfRestaurantsFiltered, "gothic_0.TTF");
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
    public void onRestart() {
        if (activityStarted) {
            count = 1;
            activityStarted = false;
            favourites = new ArrayList<>();
            getFavouritesArray();

        }
        super.onRestart();
    }

    private void getFavouritesArray() {
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userID)){
                            DataSnapshot fav = dataSnapshot.child(userID);
                            for (DataSnapshot favRestaurants : fav.getChildren()){
                                String[] info = getRestaurantInfo(favRestaurants);

                                RestaurantInfo r = new RestaurantInfo(info);
                                r.setFavourite(true);
                                r.updateDistance(lat, lng);
                                favourites.add(r);

                                if (fav.getChildrenCount() == count) {
                                    favouritesLoaded = true;
                                    Collections.sort(favourites);
                                } else {
                                    count++;
                                }
                            }
                        }
                        if (profileView) {
                            favouritesAdapter = new CustomListAdapter(ProfilePage.this, R.layout.custom_list,
                                    favourites, "gothic_0.TTF");
                            favouritesList.setAdapter(favouritesAdapter);
                            favouritesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                }
        );
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
            return new Filter() {
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
        }
    }

    private void askForPermission() {
        int permissionCheckCoarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheckFine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (!mRequestingLocationUpdates) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, this);
                        mRequestingLocationUpdates = true;
                    }
                    else{
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                        mRequestingLocationUpdates = false;
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public String[] getRestaurantInfo(DataSnapshot data){
        String [] info = new String[11];
        info[0] = data.child("rID").getValue().toString();
        info[1] = data.child("RestaurantName").getValue().toString();
        info[2] = data.child("Address").getValue().toString();
        info[3] = data.child("PhoneNumber").getValue().toString();
        info[4] = data.child("Latitude").getValue().toString();
        info[5] = data.child("Longitude").getValue().toString();
        info[6] = data.child("Website").getValue().toString();
        info[7] = data.child("Location").getValue().toString();
        info[8] = data.child("Cuisine").getValue().toString();
        info[9] = data.child("Price").getValue().toString();
        info[10] = data.child("Time").getValue().toString();

        return info;
    }

    public void getInfoPage(ListView lv){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RestaurantInfo r = (RestaurantInfo) parent.getItemAtPosition(position);
                Intent intent = new Intent(ProfilePage.this, RestaurantsInfo.class);
                intent.putExtra("rID", r.getrID());
                intent.putExtra("Name", r.getName());
                intent.putExtra("Address", r.getAddress());
                intent.putExtra("Cuisine", r.cuisineString());
                intent.putExtra("Latitude", String.valueOf(r.getLatitude()));
                intent.putExtra("Longitude", String.valueOf(r.getLongitude()));
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
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
//            stopLocationUpdates();

        } else if (profileView && favouritesLoaded && mGoogleApiClient.isConnected()) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            lat = location.getLatitude();
            lng = location.getLongitude();

            for (RestaurantInfo r : favourites) {
                r.updateDistance(lat, lng);
            }
            Collections.sort(favourites);
            favouritesAdapter.notifyDataSetChanged();
//            stopLocationUpdates();
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
//                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            ImageHelper n = new ImageHelper();
            return n.getRoundedCornerBitmap(mIcon11, 400);
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
