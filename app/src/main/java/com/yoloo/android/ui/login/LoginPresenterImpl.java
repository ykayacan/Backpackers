package com.yoloo.android.ui.login;

import com.yoloo.android.data.Repository;
import com.yoloo.android.data.model.AccountModel;
import com.yoloo.android.tardis.base.presenter.BasePresenter;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

    private static final String BUNDLE_DATA = "data";
    private static final String BUNDLE_LOAD_FINISHED = "loading_finished";

    private String text;
    private boolean loadFinished;

    private final Repository<AccountModel> mRepository;

    public LoginPresenterImpl(Repository<AccountModel> repository) {
        mRepository = repository;
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        if (bundle != null) {
            // if bundle is not empty we know that process or activity is recreated, so loader state need to be restored
            text = bundle.getString(BUNDLE_DATA);
            loadFinished = bundle.getBoolean(BUNDLE_LOAD_FINISHED);
        }
    }

    @Override
    public void onDestroy(@NonNull Bundle bundle) {
        bundle.putBoolean(BUNDLE_LOAD_FINISHED, loadFinished);
        bundle.putString(BUNDLE_DATA, text);
    }

    @Override
    public void login(boolean refresh) {
        if (getView() != null) {
            if (refresh) {
                loadFinished = false;
            }
            if (!loadFinished) {
                getView().showLoading();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFinished = true;
                        text = "Hey man! This is maniac.";
                        //Do something after 100ms
                        getView().setData(text);
                        getView().showContent();
                    }
                }, 6500);
            } else {
                getView().setData(text);
                getView().showContent();
            }
        }
    }
}
