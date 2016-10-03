package com.backpackers.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;

import com.backpackers.android.backend.model.Token;
import com.backpackers.android.backend.oauth2.OAuth;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.validator.Validator;
import com.backpackers.android.backend.validator.rule.token.ClientIdRule;
import com.backpackers.android.backend.validator.rule.token.GrantTypeRule;
import com.googlecode.objectify.VoidWork;
import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.oauth2.GrantType;
import com.backpackers.android.backend.controller.TokenController;
import com.backpackers.android.backend.validator.rule.token.TokenMissingRule;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Api(
        name = "yolooApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(
        resource = "tokens",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class TokenEndpoint {

    private static final Logger logger =
            Logger.getLogger(TokenEndpoint.class.getSimpleName());

    @ApiMethod(
            name = "oauth2.token",
            path = "oauth2/token",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Token token(final HttpServletRequest request,
                       @Named("grant_type") final String grantType,
                       @Named("username") @Nullable final String username,
                       @Named("password") @Nullable final String password,
                       @Named("refresh_token") @Nullable final String refreshToken)
            throws ServiceException {

        final String clientId = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        Validator.builder()
                .addRule(new GrantTypeRule(grantType))
                .addRule(new ClientIdRule(clientId))
                .validate();

        if (grantType.equals(GrantType.PASSWORD.toString())) {
            return TokenController.newInstance().processGrantTypePassword(username, password);
        } else if (grantType.equals(GrantType.REFRESH_TOKEN.toString())) {
            return TokenController.newInstance().processGrantTypeRefreshToken(refreshToken);
        }

        return null;
    }

    @ApiMethod(
            name = "oauth2.revoke",
            path = "oauth2/revoke",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void revokeToken(final HttpServletRequest request)
            throws ServiceException {

        final String accessToken = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        Validator.builder()
                .addRule(new TokenMissingRule(accessToken, GrantType.PASSWORD))
                .validate();

        final Token token = TokenController.getTokenByAccessToken(accessToken);

        if (token == null) {
            throw new BadRequestException("Invalid access token");
        }

        OfyHelper.ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                token.setAccessToken(null);
                OfyHelper.ofy().save().entity(token).now();
            }
        });
    }
}