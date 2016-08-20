package com.yoloo.android.data.repository;

import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.repository.remote.TokenService;

import rx.Observable;

public class TokenRepository {

    private final TokenService mService;

    public TokenRepository(TokenService mService) {
        this.mService = mService;
    }

    public Observable<Token> get(final String email, final String password) {
        return mService.getAccessToken(email, password);
    }
}
