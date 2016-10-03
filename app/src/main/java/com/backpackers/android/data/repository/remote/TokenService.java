package com.backpackers.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;

import com.backpackers.android.Constants;
import com.backpackers.android.backend.modal.yolooApi.model.Token;
import com.backpackers.android.util.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class TokenService {

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    public Observable<Token> getAccessToken(final String email, final String password) {
        return Observable.fromCallable(new Callable<Token>() {
            @Override
            public Token call() throws Exception {
                return ServerHelper.getYolooApi()
                        .oauth2()
                        .token(GRANT_TYPE_PASSWORD)
                        .setUsername(email)
                        .setPassword(password)
                        .setRequestHeaders(new HttpHeaders()
                                .setAuthorization(Constants.BASE64_CLIENT_ID))
                        .execute();
            }
        });
    }

    public Observable<Token> getAccessTokenWithRefreshToken(final String refreshToken) {
        return Observable.fromCallable(new Callable<Token>() {
            @Override
            public Token call() throws Exception {
                return ServerHelper.getYolooApi()
                        .oauth2()
                        .token(GRANT_TYPE_REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .setRequestHeaders(new HttpHeaders()
                                .setAuthorization(Constants.BASE64_CLIENT_ID))
                        .execute();
            }
        });
    }
}
