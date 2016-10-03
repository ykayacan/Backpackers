package com.backpackers.android.util;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import com.backpackers.android.BuildConfig;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class GoogleAuthHelper implements GoogleApiClient.OnConnectionFailedListener {

    // The Activity this object is bound to (we use a weak ref to avoid context leaks)
    private WeakReference<FragmentActivity> mActivityRef;

    // Callbacks interface we invoke to notify the user of this class of useful events
    private WeakReference<AuthCallbacks> mCallbacksRef;

    private GoogleApiClient mGoogleApiClient;

    public GoogleAuthHelper(FragmentActivity activity, AuthCallbacks callbacks) {
        mActivityRef = new WeakReference<>(activity);
        mCallbacksRef = new WeakReference<>(callbacks);
    }

    public void onCreate() {
        FragmentActivity activity = getActivity();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.CLIENT_SERVER_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                //.enableAutoManage(activity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /** Starts the helper. Call this from your Activity's refreshToken(). */
    public void refreshToken() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        OptionalPendingResult<GoogleSignInResult> opr =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Timber.i("Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private FragmentActivity getActivity() {
        return mActivityRef.get();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Timber.i("handleSignInResult: %s", result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            mCallbacksRef.get().onSignedIn(googleSignInAccount);
        } else {
            // Signed out, show unauthenticated UI.
            mCallbacksRef.get().onSignInFailed();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed()");
    }

    public interface AuthCallbacks {

        void onSignedIn(GoogleSignInAccount account);

        void onSignInFailed();
    }
}
