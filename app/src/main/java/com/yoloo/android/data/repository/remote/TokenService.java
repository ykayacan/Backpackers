package com.yoloo.android.data.repository.remote;

import com.google.api.client.http.HttpHeaders;
import com.yoloo.android.Constants;
import com.yoloo.android.backend.modal.yolooApi.model.Token;
import com.yoloo.android.data.ServerHelper;

import java.util.concurrent.Callable;

import rx.Observable;

public class TokenService {

    public Observable<Token> getAccessToken(final String email, final String password) {
        return Observable.fromCallable(new Callable<Token>() {
            @Override
            public Token call() throws Exception {
                return ServerHelper.getYolooApi()
                        .oauth2()
                        .token("password")
                        .setUsername(email)
                        .setPassword(password)
                        .setRequestHeaders(new HttpHeaders()
                                .setAuthorization(Constants.BASE64_CLIENT_ID))
                        .execute();
            }
        });
    }
}
