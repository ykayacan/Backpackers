package com.yoloo.android.backend.authenticator;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.repackaged.com.google.api.client.json.jackson2.JacksonFactory;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.oauth2.OAuth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class GoogleAuthenticator implements Authenticator {

    private static GoogleIdTokenVerifier getVerifier() {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(Constants.WEB_CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public static GoogleIdToken.Payload processGoogleToken(String idToken) {
        try {
            GoogleIdToken googleIdToken = getVerifier().verify(idToken);
            if (googleIdToken != null) {
                return googleIdToken.getPayload();
            }
        } catch (GeneralSecurityException | IOException ignored) {

        }

        return null;
    }

    @Override
    public User authenticate(HttpServletRequest request) {
        final String authzHeader = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        if (Strings.isNullOrEmpty(authzHeader) ||
                authzHeader.contains(OAuth.OAUTH_HEADER_NAME)) {
            return null;
        }

        GoogleIdToken.Payload payload = processGoogleToken(authzHeader);
        if (payload == null) {
            return null;
        }

        Key<Account> accountKey = getAccountKeyByEmail(payload.getEmail());
        if (accountKey == null) {
            return null;
        }

        return new User(accountKey.toWebSafeString(), "");
    }

    private static Key<Account> getAccountKeyByEmail(String email) {
        return ofy().load().type(Account.class)
                .filter("email", new Email(email))
                .keys().first().now();
    }
}
