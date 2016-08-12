package com.yoloo.android.ui.login;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.yoloo.android.data.model.AccountModel;
import com.yoloo.android.data.repository.Repository;

import android.content.Intent;
import android.support.annotation.NonNull;

import timber.log.Timber;

class LoginPresenter extends MvpBasePresenter<LoginView> {

    private final Repository<AccountModel> mRepository;

    public LoginPresenter(Repository<AccountModel> repository) {
        mRepository = repository;
    }

    public void loginByGoogle(GoogleApiClient client) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        if (getView() != null) {
            getView().onStartActivityForResult(signInIntent);
        }
    }

    public void silentLoginByGoogle(GoogleApiClient client) {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(client);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Timber.d("Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            if (getView() != null) {
                getView().onShowProgressDialog(true);
            }
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    if (getView() != null) {
                        getView().onShowProgressDialog(false);
                        handleSignInResult(googleSignInResult);
                    }
                }
            });
        }
    }

    public void loginByFacebook() {

    }

    public void loginByYoloo(String username, String email, String password) {
        /*AccountModel account = new AccountModel.Builder()
                .setAccountType(AccountModel.YOLOO)
                .setUsername(username)
                .setEmail(email)
                .setPassword(password)
                .setPictureFile()
                .build();
        mRepository.save(account);*/
    }

    public void processGoogleLogin(Intent intent) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        handleSignInResult(result);
    }

    public void logOut(GoogleApiClient client) {
        Auth.GoogleSignInApi.signOut(client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        //updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Timber.d("token: %s", acct.getIdToken());

            AccountModel account = new AccountModel.Builder()
                    .setAccountType(AccountModel.GOOGLE)
                    .setAccessToken(acct.getIdToken())
                    .build();

            mRepository.create(account);

            if (getView() != null) {
                getView().navigateToHome();
            }
        } else {
            // Signed out, show unauthenticated UI.
        }
    }
}
