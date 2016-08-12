package com.yoloo.android.ui.login;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.yoloo.android.BuildConfig;
import com.yoloo.android.R;
import com.yoloo.android.data.repository.AccountRepository;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends MvpActivity<LoginView, LoginPresenter> implements
        LoginView, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_GOOGLE_LOGIN = 9001;

    @BindView(R.id.google_login)
    SignInButton mSignInButton;

    @BindView(R.id.logout)
    Button mLogoutButton;

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.email)
    EditText mEmail;

    @BindView(R.id.password)
    EditText mPassword;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setupGoogleLogin();
    }

    @Override
    public void onStart() {
        super.onStart();
        getPresenter().silentLoginByGoogle(mGoogleApiClient);
    }

    @NonNull
    @Override
    public LoginPresenter createPresenter() {
        return new LoginPresenter(new AccountRepository());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_LOGIN) {
            getPresenter().processGoogleLogin(data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @OnClick(R.id.google_login)
    void signIn() {
        getPresenter().loginByGoogle(mGoogleApiClient);
    }

    @OnClick(R.id.logout)
    void logOut() {
        getPresenter().logOut(mGoogleApiClient);
    }

    @OnClick(R.id.register)
    void register() {
        String username = mUsername.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        getPresenter().loginByYoloo(username, email, password);
        mUsername.setText("");
        mEmail.setText("");
        mPassword.setText("");
    }

    @Override
    public void onShowProgressDialog(boolean show) {
        if (show) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Loading...");
                mProgressDialog.setIndeterminate(true);
            }

            mProgressDialog.show();
        } else {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }

    @Override
    public void navigateToHome() {

    }

    @Override
    public void onStartActivityForResult(Intent intent) {
        startActivityForResult(intent, RC_GOOGLE_LOGIN);
    }

    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.CLIENT_SERVER_ID)
                .requestEmail()
                //.requestScopes((new Scope(Scopes.PLUS_LOGIN)), new Scope(Scopes.PLUS_ME))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setScopes(gso.getScopeArray());
    }
}
