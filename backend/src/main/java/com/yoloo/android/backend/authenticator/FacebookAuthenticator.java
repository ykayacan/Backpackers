package com.yoloo.android.backend.authenticator;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;

import javax.servlet.http.HttpServletRequest;

public class FacebookAuthenticator implements Authenticator {

    @Override
    public User authenticate(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        return null;
    }
}
