package com.backpackers.android.data.repository;

import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.data.repository.remote.TokenService;

import rx.Observable;

public class TokenRepository {

    private final TokenService mService;

    public TokenRepository(TokenService mService) {
        this.mService = mService;
    }

    public Observable<Token> getAccessToken(final String email, final String password) {
        return mService.getAccessToken(email, password);
    }

    public Observable<Token> getAccessTokenWithRefreshToken(final String refreshToken) {
        return mService.getAccessTokenWithRefreshToken(refreshToken);
    }
}
