package com.backpackers.android.backend.authenticator;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;

import com.backpackers.android.backend.controller.TokenController;
import com.backpackers.android.backend.model.Token;
import com.backpackers.android.backend.oauth2.OAuth;

import javax.servlet.http.HttpServletRequest;

public class YolooAuthenticator implements Authenticator {

    @Override
    public User authenticate(HttpServletRequest request) {
        final String authzHeader = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        if (Strings.isNullOrEmpty(authzHeader) ||
                !authzHeader.contains(OAuth.OAUTH_HEADER_NAME)) {
            return null;
        }

        final String accessToken = authzHeader.substring(6).trim();

        Token token = TokenController.getTokenByAccessToken(accessToken);
        if (token == null || token.isTokenExpired()) {
            return null;
        }

        return new User(token.getKey().getParent().toWebSafeString(), "");
    }
}
