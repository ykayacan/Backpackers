package com.backpackers.android.ui.base;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.Constants;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.TokenRepository;
import com.backpackers.android.data.repository.remote.TokenService;
import com.backpackers.android.framework.base.BaseMvpActivity;
import com.backpackers.android.framework.base.BaseMvpPresenter;
import com.backpackers.android.framework.base.BaseMvpView;
import com.backpackers.android.ui.signin.SignInActivity;
import com.backpackers.android.util.AuthUtils;
import com.backpackers.android.util.GoogleAuthHelper;
import com.backpackers.android.util.PrefUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;

import butterknife.ButterKnife;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseAuthActivity<V extends BaseMvpView, P extends BaseMvpPresenter<V>>
        extends BaseMvpActivity<V, P> implements GoogleAuthHelper.AuthCallbacks {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private GoogleAuthHelper mGoogleAuthHelper;
    private Subscription mTokenSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthUtils.shouldNavigateToSignIn(this)) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (AuthUtils.isProviderGoogle(this)) {
            mGoogleAuthHelper = new GoogleAuthHelper(this, this);
            mGoogleAuthHelper.onCreate();
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    public void onPause() {
        if (mTokenSubscription != null && !mTokenSubscription.isUnsubscribed()) {
            mTokenSubscription.unsubscribe();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (AuthUtils.isProviderGoogle(this)) {
            mGoogleAuthHelper.onStop();
        }
        super.onStop();
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        PrefUtils.with(this).edit()
                .putString(Constants.PREF_KEY_ACCESS_TOKEN, account.getIdToken())
                .commit();
    }

    @Override
    public void onSignInFailed() {

    }

    protected final void refreshToken() {
        if (AuthUtils.isProviderGoogle(this)) {
            mGoogleAuthHelper.refreshToken();
        } else if (AuthUtils.isProviderYoloo(this)) {
            final String refreshToken = PrefUtils.with(this)
                    .getString(Constants.PREF_KEY_REFRESH_TOKEN, "");
            mTokenSubscription = new TokenRepository(new TokenService())
                    .getAccessTokenWithRefreshToken(refreshToken)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Token>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Token token) {
                            Timber.d("token: %s", token.getAccessToken());
                            PrefUtils.with(getApplicationContext())
                                    .edit().putString(Constants.PREF_KEY_REFRESH_TOKEN, "")
                                    .apply();
                        }
                    });
        } else {
            Timber.e("AuthProvider not found.");
        }
    }

    protected final char[] getAccessToken() {
        return PrefUtils.with(this)
                .getString(Constants.PREF_KEY_ACCESS_TOKEN, "").toCharArray();
    }

    protected final String getProfileImageUrl() {
        return PrefUtils.with(this).getString(Constants.PREF_KEY_PROFILE_IMAGE_URL, "");
    }

    protected final String getUsername() {
        return PrefUtils.with(this).getString(Constants.PREF_KEY_USERNAME, "");
    }

    protected final String getEmail() {
        return PrefUtils.with(this).getString(Constants.PREF_KEY_EMAIL, "");
    }

    protected final String getUserId() {
        return PrefUtils.with(this).getString(Constants.PREF_KEY_USER_ID, "");
    }
}
