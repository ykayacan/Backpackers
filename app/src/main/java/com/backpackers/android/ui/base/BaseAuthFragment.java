package com.backpackers.android.ui.base;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.backpackers.android.Constants;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.TokenRepository;
import com.backpackers.android.data.repository.remote.TokenService;
import com.backpackers.android.framework.base.BaseMvpFragment;
import com.backpackers.android.framework.base.BaseMvpPresenter;
import com.backpackers.android.framework.base.BaseMvpView;
import com.backpackers.android.util.AuthUtils;
import com.backpackers.android.util.GoogleAuthHelper;
import com.backpackers.android.util.PrefUtils;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseAuthFragment<V extends BaseMvpView, P extends BaseMvpPresenter<V>>
        extends BaseMvpFragment<V, P> implements GoogleAuthHelper.AuthCallbacks {

    private GoogleAuthHelper mGoogleAuthHelper;
    private Subscription mTokenSubscription;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayoutResId(), container, false);
        bindViews(view);
        return view;
    }

    /**
     * Every fragment has to inflate a layout in the onCreateView method. We have added this method
     * to avoid duplicate all the inflate code in every fragment. You only have to return the
     * layout to inflate in this method when extends BaseFragment.
     */
    @LayoutRes
    protected abstract int getFragmentLayoutResId();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (AuthUtils.isProviderGoogle(getContext())) {
            mGoogleAuthHelper = new GoogleAuthHelper(getActivity(), this);

            mGoogleAuthHelper.onCreate();
        }
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
        if (AuthUtils.isProviderGoogle(getContext())) {
            mGoogleAuthHelper.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onSignedIn(GoogleSignInAccount account) {
        PrefUtils.with(getContext()).edit()
                .putString(Constants.PREF_KEY_ACCESS_TOKEN, account.getIdToken())
                .commit();
    }

    @Override
    public void onSignInFailed() {

    }

    protected final void refreshToken() {
        Timber.d("refreshToken");
        if (AuthUtils.isProviderGoogle(getContext())) {
            mGoogleAuthHelper.refreshToken();
        } else if (AuthUtils.isProviderYoloo(getContext())) {
            final String refreshToken = PrefUtils.with(getContext())
                    .getString(Constants.PREF_KEY_REFRESH_TOKEN, "");
            Timber.d("Refresh token: %s", refreshToken);
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
                            Timber.d("Refresh error: %s", e.getMessage());
                        }

                        @Override
                        public void onNext(Token token) {
                            Timber.d("token: %s", token.getAccessToken());
                            PrefUtils.with(getContext())
                                    .edit().putString(Constants.PREF_KEY_REFRESH_TOKEN, "")
                                    .apply();
                        }
                    });
        } else {
            Timber.e("AuthProvider not found.");
        }
    }

    protected final char[] getAccessToken() {
        return PrefUtils.with(getContext())
                .getString(Constants.PREF_KEY_ACCESS_TOKEN, "").toCharArray();
    }

    protected final String getProfileImageUrl() {
        return PrefUtils.with(getContext()).getString(Constants.PREF_KEY_PROFILE_IMAGE_URL, "");
    }

    protected final String getUsername() {
        return PrefUtils.with(getContext()).getString(Constants.PREF_KEY_USERNAME, "");
    }

    protected final String getUserId() {
        return PrefUtils.with(getContext()).getString(Constants.PREF_KEY_USER_ID, "");
    }

    /**
     * Replace every field annotated with ButterKnife annotations like @BindView with the proper
     * value.
     *
     * @param view to extract each widget injected in the fragment.
     */
    private void bindViews(final View view) {
        mUnbinder = ButterKnife.bind(this, view);
    }
}
