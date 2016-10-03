package com.backpackers.android.backend.controller;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.Email;

import com.backpackers.android.backend.model.Token;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.validator.Validator;
import com.backpackers.android.backend.validator.rule.token.PasswordGrantTypeRule;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.oauth2.GrantType;
import com.backpackers.android.backend.util.RandomGenerator;
import com.backpackers.android.backend.validator.rule.token.TokenMissingRule;

import java.util.Date;

public class TokenController {

    public static TokenController newInstance() {
        return new TokenController();
    }

    public static Token getTokenByAccessToken(String accessToken) {
        return OfyHelper.ofy().load().type(Token.class)
                .filter("accessToken =", accessToken)
                .first().now();
    }

    private static Token getTokenByRefreshToken(String refreshToken) {
        return OfyHelper.ofy().load().type(Token.class)
                .filter("refreshToken =", refreshToken)
                .first().now();
    }

    public Token processGrantTypePassword(final String username, final String password)
            throws ServiceException {
        Validator.builder()
                .addRule(new PasswordGrantTypeRule(username, password))
                .validate();

        final Account account = getAccountByEmail(username);

        if (account == null) {
            throw new BadRequestException("Invalid email");
        }

        if (!account.isValidPassword(password)) {
            throw new BadRequestException("Invalid password");
        }

        if (isTokenExists(account)) {
            return OfyHelper.ofy().load().type(Token.class).ancestor(account)
                    .first().now();
        }

        final Token token = Token.builder(account.getKey())
                .setAccessToken(RandomGenerator.INSTANCE.generate())
                .setRefreshToken(RandomGenerator.INSTANCE.generate())
                .build();

        return OfyHelper.ofy().transact(new Work<Token>() {
            @Override
            public Token run() {
                Key<Token> tokenKey = OfyHelper.ofy().save().entity(token).now();

                return OfyHelper.ofy().load().type(Token.class)
                        .parent(tokenKey.getParent()).id(tokenKey.getId()).now();
            }
        });
    }

    public Token processGrantTypeRefreshToken(String refreshToken)
            throws ServiceException {
        Validator.builder()
                .addRule(new TokenMissingRule(refreshToken, GrantType.REFRESH_TOKEN))
                .validate();

        final Token token = getTokenByRefreshToken(refreshToken);

        if (token == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        return OfyHelper.ofy().transact(new Work<Token>() {
            @Override
            public Token run() {
                token.setAccessToken(RandomGenerator.INSTANCE.generate());
                token.setExpiresIn(Constants.TOKEN_EXPIRES_IN);
                token.setCreatedAt(new Date());

                Key<Token> tokenKey = OfyHelper.ofy().save().entity(token).now();

                return OfyHelper.ofy().load().key(tokenKey).now();
            }
        });
    }

    private Account getAccountByEmail(String email) {
        return OfyHelper.ofy().load().type(Account.class)
                .filter("email =", new Email(email))
                .first().now();
    }

    private boolean isTokenExists(Account account) {
        return OfyHelper.ofy().load().type(Token.class).ancestor(account)
                .keys().first().now() != null;
    }
}
