package mavlana.halaleats;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import android.content.Intent;
import com.google.android.gms.common.SignInButton;

import org.json.JSONObject;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work with your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends FragmentActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private CallbackManager callbackManager;
    private boolean loggedIn = false;

    private String name;

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        if (isLoggedIn()){
            GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            if (response.getError() != null) {
                                // handle error
                            } else {
                                name = me.optString("name");
                                Intent connected = new Intent(LoginActivity.this, ProfilePage.class).putExtra("Login Type", "Facebook");
                                connected.putExtra("ID", AccessToken.getCurrentAccessToken().getUserId());
                                connected.putExtra("Name", name);
                                startActivity(connected);
                                finish();
                                // send email and id to your web server
                            }
                        }
                    }).executeAsync();
        }
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                } else {
                                    name = me.optString("name");
                                    Intent connected = new Intent(LoginActivity.this, ProfilePage.class).putExtra("Login Type", "Facebook");
                                    connected.putExtra("ID", loginResult.getAccessToken().getUserId());
                                    connected.putExtra("Name", name);
                                    startActivity(connected);
                                    finish();
                                    // send email and id to your web server
                                }
                            }
                        }).executeAsync();


            }

            @Override
            public void onCancel() {
                System.out.println("Error");
            }

            @Override
            public void onError(FacebookException e) {
                System.out.println("Error");

            }
        });

        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }
        // [END restore_saved_instance_state]

        // Set up button click listeners
        findViewById(R.id.plus_sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);


        // Large sign-in
        ((SignInButton) findViewById(R.id.plus_sign_in_button)).setSize(SignInButton.SIZE_WIDE);

        // Start with sign-in button disabled until sign-in either succeeds or fails
//        findViewById(R.id.plus_sign_in_button).setEnabled(false);

        // [START create_google_api_client]
        // Build GoogleApiClient with access to basic profile
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END create_google_api_client]
        SignInButton signInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            // Set button visibility
            findViewById(R.id.plus_sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            // Show signed-out message

            // Set button visibility
            findViewById(R.id.plus_sign_in_button).setEnabled(true);
            findViewById(R.id.plus_sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    // [START on_start_on_stop]
    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    // [END on_start_on_stop]

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }
    // [END on_save_instance_state]

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

//            mIsResolving = false;
//            mGoogleApiClient.connect();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }
    // [END on_activity_result]

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Intent connected = new Intent(this, ProfilePage.class).putExtra("Login Type", "Google");
            try {
                connected.putExtra("Name", acct.getDisplayName());
            }
            catch (NullPointerException e){
                connected.putExtra("Name", "Unknown User");
            }
            connected.putExtra("ID", acct.getId());
            try {
                connected.putExtra("Image", acct.getPhotoUrl().toString());
            }
            catch (NullPointerException e) {
                connected.putExtra("Image", "https://docs.google.com/uc?authuser=0&id=0BwLvksCPOPZEQkRrMmVQYUx4am8&export=download");
            }
            startActivity(connected);
            finish();
//            updateUI(true);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);

        Intent connected = new Intent(this, ProfilePage.class).putExtra("Login Type", "Google");
        startActivity(connected);
        finish();
        // Show the signed-in UI
//        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
//                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI(false);
        }
    }
    // [END on_connection_failed]
//
//    private void showErrorDialog(ConnectionResult connectionResult) {
//        int errorCode = connectionResult.getErrorCode();
//
//        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
//            // Show the default Google Play services error dialog which may still start an intent
//            // on our behalf if the user can resolve the issue.
//            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
//                    new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            mShouldResolve = false;
//                            updateUI(false);
//                        }
//                    }).show();
//        } else {
//            // No default Google Play Services error, display a message to the user.
////            String errorString = getString(R.string.play_services_error_fmt, errorCode);
//
//            mShouldResolve = false;
//            updateUI(false);
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plus_sign_in_button:
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
//                mStatus.setText(R.string.signing_in);
                // [START sign_in_clicked]
//                mShouldResolve = true;
//                mGoogleApiClient.connect();
                // [END sign_in_clicked]
                // [END disconnect_clicked]
//                updateUI(false);
                signIn();
                break;
            case R.id.sign_out_button:
                // Clear the default account so that GoogleApiClient will not automatically
                // connect in the future.
                // [START sign_out_clicked]
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                // [END sign_out_clicked]
                updateUI(false);
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public boolean isLoggedIn() {
        if(AccessToken.getCurrentAccessToken()!=null)
            loggedIn=true;
        return loggedIn;

    }
}