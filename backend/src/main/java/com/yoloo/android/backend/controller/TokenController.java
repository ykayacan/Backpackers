package com.yoloo.android.backend.controller;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.Email;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.model.Token;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.oauth2.GrantType;
import com.yoloo.android.backend.util.RandomGenerator;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.token.PasswordGrantTypeRule;
import com.yoloo.android.backend.validator.rule.token.TokenMissingRule;

import java.util.Date;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class TokenController {

    public static TokenController newInstance() {
        return new TokenController();
    }

    public static Token getTokenByAccessToken(String accessToken) {
        return ofy().load().type(Token.class)
                .filter("accessToken =", accessToken)
                .first().now();
    }

    private static Token getTokenByRefreshToken(String refreshToken) {
        return ofy().load().type(Token.class)
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
            return ofy().load().type(Token.class).ancestor(account)
                    .first().now();
        }

        final Token token = Token.builder(account.getKey())
                .setAccessToken(RandomGenerator.INSTANCE.generate())
                .setRefreshToken(RandomGenerator.INSTANCE.generate())
                .build();

        return ofy().transact(new Work<Token>() {
            @Override
            public Token run() {
                Key<Token> tokenKey = ofy().save().entity(token).now();

                return ofy().load().type(Token.class)
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

        return ofy().transact(new Work<Token>() {
            @Override
            public Token run() {
                token.setAccessToken(RandomGenerator.INSTANCE.generate());
                token.setExpiresIn(Constants.TOKEN_EXPIRES_IN);
                token.setCreatedAt(new Date());

                Key<Token> tokenKey = ofy().save().entity(token).now();

                return ofy().load().type(Token.class)
                        .parent(tokenKey.getParent()).id(tokenKey.getId()).now();
            }
        });
    }

    private Account getAccountByEmail(String email) {
        return ofy().load().type(Account.class)
                .filter("email =", new Email(email))
                .first().now();
    }

    private boolean isTokenExists(Account account) {
        return ofy().load().type(Token.class).ancestor(account)
                .keys().first().now() != null;
    }
}
