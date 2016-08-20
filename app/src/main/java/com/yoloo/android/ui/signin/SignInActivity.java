package com.yoloo.android.ui.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.yoloo.android.BuildConfig;
import com.yoloo.android.R;
import com.yoloo.android.backend.modal.yolooApi.model.Account;
import com.yoloo.android.data.repository.AccountRepository;
import com.yoloo.android.data.repository.remote.UserService;
import com.yoloo.android.ui.signin.yoloo.YolooSignInActivity;
import com.yoloo.android.ui.signup.YolooSignUpActivity;
import com.yoloo.android.util.NetworknUtil;
import com.yoloo.android.util.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SignInActivity extends MvpActivity<SignInView, SignInPresenter> implements
        SignInView, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    @BindView(R.id.loginGoogleSignInBtn)
    SignInButton mSignInButton;

    @BindView(R.id.rootViewCL)
    ConstraintLayout mRootView;

    private GoogleApiClient mGoogleApiClient;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        setupGoogleSignIn();
    }

    @Override
    public void onStart() {
        super.onStart();
        //setupGoogleSilentSignIn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    @NonNull
    @Override
    public SignInPresenter createPresenter() {
        return new SignInPresenter(new AccountRepository(new UserService()));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("Connection error: %s", connectionResult.getErrorMessage());
    }

    @Override
    public void onNavigateToHome() {
        Timber.d("Navigating to home.");
    }

    @Override
    public void onSaveUser(Account account) {
        Prefs.with(this, "user")
                .edit()
                .putString("id", account.getId())
                .putString("email", account.getEmail())
                .putString("usename", account.getUsername())
                .putString("profileImageUrl", account.getProfileImageUrl())
                .putString("provider", account.getProvider())
                .apply();
    }

    @Override
    public void onEmailExistsError() {
        Snackbar.make(mRootView, R.string.error_email_already_taken, Snackbar.LENGTH_SHORT)
                .show();
    }

    @OnClick(R.id.loginYolooSignInBtn)
    void navigateToYolooSignIn() {
        if (!NetworknUtil.isConnected(getApplicationContext())) {
            Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Intent intent = new Intent(this, YolooSignInActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }

    @OnClick(R.id.loginYolooSignUpBtn)
    void navigateToYolooSignUp() {
        if (!NetworknUtil.isConnected(getApplicationContext())) {
            Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT)
                    .show();
        } else {
            Intent intent = new Intent(this, YolooSignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
    }

    @OnClick(R.id.loginGoogleSignInBtn)
    void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Timber.d("Signed out.");
                    }
                });
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            getPresenter().sendGoogleToken(acct.getIdToken());
        }
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void setupGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.CLIENT_SERVER_ID)
                .requestEmail()
                //.requestScopes((new Scope(Scopes.PLUS_LOGIN)), new Scope(Scopes.PLUS_ME))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setScopes(gso.getScopeArray());
    }

    private void setupGoogleSilentSignIn() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleGoogleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleGoogleSignInResult(googleSignInResult);
                }
            });
        }
    }
}
