package com.yoloo.android.backend.authenticator;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.repackaged.com.google.api.client.json.jackson2.JacksonFactory;

import com.yoloo.android.backend.modal.Account;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class GoogleAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(GoogleAuthenticator.class.getName());

    @Override
    public User authenticate(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null) {
            String userEmail = processGoogleToken(token);
            if (userEmail != null) {
                return new User(userEmail);
            }
        }
        return null;
    }

    private String processGoogleToken(String idToken) {
        NetHttpTransport transport = new NetHttpTransport();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport,
                new JacksonFactory())
                // TODO: 1.06.2016 add audience
                .setAudience(Collections.singletonList(""))
                .setIssuer("https://accounts.google.com")
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                saveProfile(googleIdToken.getPayload());
                return googleIdToken.getPayload().getEmail();
            }
        } catch (GeneralSecurityException | IOException e) {
            logger.info(e.getMessage());
        }

        return null;
    }

    private void saveProfile(GoogleIdToken.Payload payload) {
        Account a = new Account.Builder()
                .setUsername((String) payload.get("name"))
                .setRealname((String) payload.get("given_name"))
                .setEmail(payload.getEmail())
                .setPictureUrl((String) payload.get("picture"))
                .build();

        ofy().save().entity(a).now();
    }
}
