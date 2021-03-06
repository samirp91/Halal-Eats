package mavlana.halaleats;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RestaurantsInfo extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private String name;
    private String address;
    private String cuisine;
    private String lat;
    private String lng;
    private String number;
    private String web;
    private String hours;
    private String myLat;
    private String myLng;
    private String price;
    private String location;
    private ImageButton bookmarkBtn;
    private boolean favourite;
    private String userID;
    private String rID;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://flickering-inferno-2585.firebaseio.com/");

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private Location mCurrentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_info);
        name = getIntent().getStringExtra("Name");
        address = getIntent().getStringExtra("Address");
        cuisine = getIntent().getStringExtra("Cuisine");
        lat = getIntent().getStringExtra("Latitude");
        lng = getIntent().getStringExtra("Longitude");
        number = getIntent().getStringExtra("Number");
        web = getIntent().getStringExtra("Web");
        hours = getIntent().getStringExtra("Hours");
        myLat = getIntent().getStringExtra("MyLatitude");
        myLng = getIntent().getStringExtra("MyLongitude");
        userID = getIntent().getStringExtra("ID");
        rID = getIntent().getStringExtra("rID");
        location = getIntent().getStringExtra("Location");
        favourite = getIntent().getBooleanExtra("Favourite", false);
        price = getIntent().getStringExtra("Price");
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView nameText = (TextView) findViewById(R.id.restaurant_name);
        TextView addressText = (TextView) findViewById(R.id.restaurant_address);
        TextView cuisineText = (TextView) findViewById(R.id.restaurant_cuisine);
        final TextView timings = (TextView) findViewById(R.id.hours);
        ImageButton directionsBtn = (ImageButton) findViewById(R.id.directionsBtn);
        ImageButton callBtn = (ImageButton) findViewById(R.id.callBtn);
        bookmarkBtn = (ImageButton) findViewById(R.id.bookmarkBtn);
        ImageButton websiteBtn = (ImageButton) findViewById(R.id.websiteBtn);

        if (favourite) {
            bookmarkBtn.setImageResource(R.drawable.ic_star_rate_black_18dp);
        }

        nameText.setText(name);
        addressText.setText(address);
        cuisineText.setText(cuisine);
        timings.setText(hours);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        mGoogleApiClient.connect();
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        bookmarkBtn.setBackgroundColor(Color.TRANSPARENT);
        callBtn.setBackgroundColor(Color.TRANSPARENT);
        directionsBtn.setBackgroundColor(Color.TRANSPARENT);
        websiteBtn.setBackgroundColor(Color.TRANSPARENT);


        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference userFavourite = ref.child(userID);

                if (favourite) {
                    bookmarkBtn.setImageResource(R.drawable.ic_star_border_black_18dp);
                    userFavourite.child(rID).removeValue();
                    favourite = false;
                } else {
                    bookmarkBtn.setImageResource(R.drawable.ic_star_rate_black_18dp);
                    userFavourite.child(rID).child("rID").setValue(rID);
                    userFavourite.child(rID).child("Address").setValue(address);
                    userFavourite.child(rID).child("Cuisine").setValue(cuisine);
                    userFavourite.child(rID).child("Latitude").setValue(lat);
                    userFavourite.child(rID).child("Longitude").setValue(lng);
                    userFavourite.child(rID).child("Location").setValue(location);
                    userFavourite.child(rID).child("PhoneNumber").setValue(number);
                    userFavourite.child(rID).child("Price").setValue(price);
                    userFavourite.child(rID).child("RestaurantName").setValue(name);
                    userFavourite.child(rID).child("Time").setValue(hours);
                    userFavourite.child(rID).child("Website").setValue(web);
                    favourite = true;
                }

                userFavourite.push();

            }
        });


        directionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLat.matches("(0.0)*") && myLng.matches("(0.0)*")) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RestaurantsInfo.this);

                    //Setting Dialog Title
                    alertDialog.setTitle("No Location Set");

                    //Setting Dialog Message
                    alertDialog.setMessage("To get directions, please turn on location ");

                    //On Pressing Setting button
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    alertDialog.show();
                } else {
                    address = address.replace(' ', '+');
                    location = location.replace(' ', '+');
                    Uri directions = Uri.parse("http://maps.google.com/maps?saddr=" +
                            myLat + "," + myLng + "&daddr=" + address + ",+" + location);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, directions);
                    startActivity(mapIntent);
                }
            }
        });

        if (number.equals(" ")) {
            callBtn.setVisibility(View.INVISIBLE);
        } else {
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri phone = Uri.parse("tel:" + number);
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, phone);
                    startActivity(callIntent);
                }
            });

        }

        if (web.equals(" ")) {
            websiteBtn.setVisibility(View.INVISIBLE);
        } else {
            websiteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri website = Uri.parse("http://" + web);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, website);
                    startActivity(webIntent);
                }
            });

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_restaurants_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(lat), Double.valueOf(lng)), 15));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
                .title(name))
                .setPosition(new LatLng(Double.valueOf(lat), Double.valueOf(lng)));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (myLat.equals("0") && myLng.equals("0")) {
            startLocationUpdates();
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mCurrentLocation != null) {
                    myLat = String.valueOf(mCurrentLocation.getLatitude());
                    myLng = String.valueOf(mCurrentLocation.getLongitude());
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mGoogleApiClient.isConnected()) {
            myLat = String.valueOf(location.getLatitude());
            myLng = String.valueOf(location.getLongitude());
            stopLocationUpdates();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

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

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

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
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
}
