package com.backpackers.android.ui.signin;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import com.backpackers.android.BuildConfig;
import com.backpackers.android.Constants;
import com.backpackers.android.R;
import com.backpackers.android.backend.modal.yolooApi.model.Account;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.UserRepository;
import com.backpackers.android.data.repository.remote.UserService;
import com.backpackers.android.framework.base.BaseMvpFragment;
import com.backpackers.android.ui.home.HomeActivity;
import com.backpackers.android.util.GoogleAuthHelper;
import com.backpackers.android.util.NetworkUtil;
import com.backpackers.android.util.PrefUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

public class ProviderSignInFragment extends BaseMvpFragment<SignInView, SignInPresenter> implements
        SignInView, GoogleAuthHelper.AuthCallbacks {

    public static final String TAG = ProviderSignInFragment.class.getName();

    private static final int RC_SIGN_IN = 9001;

    @BindView(R.id.btn_google_sign_in)
    SignInButton mGoogleSignInButton;

    @BindView(R.id.root_main)
    ConstraintLayout mRootView;

    private GoogleApiClient mGoogleApiClient;

    private Unbinder mUnbinder;

    public ProviderSignInFragment() {
        // Required empty public constructor
    }

    public static ProviderSignInFragment newInstance() {
        return new ProviderSignInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_provider_sign_in, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        setupGoogleSignIn();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public SignInPresenter createPresenter() {
        return new SignInPresenter(
                new UserRepository(new UserService()));
    }

    @Override
    public void onSaveUser(Account account) {
        PrefUtils.with(getContext()).edit()
                .putString(Constants.PREF_KEY_USER_ID, account.getId())
                .putString(Constants.PREF_KEY_EMAIL, account.getEmail())
                .putString(Constants.PREF_KEY_USERNAME, account.getUsername())
                .putString(Constants.PREF_KEY_PROFILE_IMAGE_URL, account.getProfileImageUrl())
                .putString(Constants.PREF_KEY_PROVIDER, account.getProvider())
                .apply();
    }

    @Override
    public void onSaveToken(Token token) {
        PrefUtils.with(getContext()).edit()
                .putString(Constants.PREF_KEY_ACCESS_TOKEN, token.getAccessToken())
                .apply();
    }

    @Override
    public void onSuccess() {
        final Intent intent = new Intent(getActivity(), HomeActivity.class);
        // set the new task and clear flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onShowProgress(boolean show, boolean isSignIn) {

    }

    @Override
    public void onEmailExistsError(@AuthProvider int provider) {
        Snackbar.make(mRootView, R.string.error_email_already_taken, Snackbar.LENGTH_SHORT)
                .show();

        switch (provider) {
            case AuthProvider.PROVIDER_GOOGLE:
                signOutGoogle();
                break;
        }
    }

    @Override
    public void onUsernameExistsError() {

    }

    @Override
    public void onNetworkError() {
        Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUnauthorized() {

    }

    @Override
    public void onInvalidEmail() {

    }

    @Override
    public void onInvalidPassword() {

    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        getPresenter().sendGoogleToken(account.getIdToken(), Locale.getDefault().getDisplayLanguage());
    }

    @Override
    public void onSignInFailed() {

    }

    @OnClick(R.id.btn_yoloo_sign_in)
    void navigateToYolooSignIn() {
        if (!NetworkUtil.isConnected(getContext())) {
            Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT).show();
            return;
        }
        replaceFragment(YolooSignInFragment.SIGN_IN);
    }

    @OnClick(R.id.btn_yoloo_sign_up)
    void navigateToYolooSignUp() {
        if (!NetworkUtil.isConnected(getContext())) {
            Snackbar.make(mRootView, R.string.error_network_not_found, Snackbar.LENGTH_SHORT).show();
            return;
        }
        replaceFragment(YolooSignInFragment.SIGN_UP);
    }

    @OnClick(R.id.btn_google_sign_in)
    void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                getPresenter().sendGoogleToken(acct.getIdToken(), Locale.getDefault().getDisplayLanguage());
            }
        } else {
            // Signed out, show unauthenticated UI.
            Timber.d("Signed out! %s", result.getStatus().getStatusMessage());
        }
    }

    private void signOutGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.CLIENT_SERVER_ID)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(), null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSignInButton.setSize(SignInButton.SIZE_WIDE);
    }

    private void replaceFragment(int mode) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
                        R.anim.slide_left_in, R.anim.slide_right_out)
                .replace(R.id.fragment_container, YolooSignInFragment.newInstance(mode))
                .addToBackStack(null)
                .commit();
    }
}
