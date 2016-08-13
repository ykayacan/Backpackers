package com.yoloo.android.backend.api;

import com.google.api.client.util.Base64;
import com.google.api.client.util.StringUtils;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.controller.UserController;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.oauth2.GrantType;
import com.yoloo.android.backend.oauth2.OAuth;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;
import com.yoloo.android.backend.validator.rule.credentials.CredentialsExistenceRule;
import com.yoloo.android.backend.validator.rule.credentials.CredentialsRule;
import com.yoloo.android.backend.validator.rule.token.TokenMissingRule;

import java.util.logging.Logger;

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
        resource = "users",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID,},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class
        }
)
public class UserEndpoint {

    private static final Logger logger = Logger.getLogger(UserEndpoint.class.getSimpleName());

    /**
     * Returns the {@link Account} with the corresponding ID.
     *
     * @param websafeUserId the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "users.get",
            path = "users/{userId}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Account get(@Named("userId") final String websafeUserId, final User user)
            throws ServiceException {
        Validator.builder()
                .addRule(new IdValidationRule(websafeUserId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeUserId))
                .validate();

        return UserController.newInstance().get(websafeUserId);
    }

    /**
     * Returns the {@link Account}.
     *
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "users.me",
            path = "users/me",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Account getMe(final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return UserController.newInstance().get(user.getUserId());
    }

    @ApiMethod(
            name = "users.private.google",
            path = "users/private/google",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account createGoogleAccount(final HttpServletRequest request)
            throws ServiceException {
        String accessToken = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        // Validate.
        Validator.builder()
                .addRule(new TokenMissingRule(accessToken, GrantType.PASSWORD))
                .validate();

        Payload payload = GoogleAuthenticator.processGoogleToken(accessToken);
        if (payload != null) {
            return UserController.newInstance().add(payload);
        }
        return null;
    }

    @ApiMethod(
            name = "users.private.facebook",
            path = "users/private/facebook",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account createFacebookAccount(final HttpServletRequest request)
            throws ServiceException {
        String accessToken = request.getHeader(OAuth.HeaderType.AUTHORIZATION);

        // Validate.
        Validator.builder()
                .addRule(new TokenMissingRule(accessToken, GrantType.PASSWORD))
                .validate();

        // TODO: 26.06.2016 Implement facebook account.
        return null;
    }

    @ApiMethod(
            name = "users.private.yoloo",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account createYolooAccount(@Named("credentials") final String credentials)
            throws ServiceException {
        Validator.builder()
                .addRule(new CredentialsRule(credentials))
                .validate();

        final String decodedCredentials =
                StringUtils.newStringUtf8(Base64.decodeBase64(credentials));
        // credentials pattern = username:password:email
        final String[] values = decodedCredentials.split("\\s*:\\s*");

        Validator.builder()
                .addRule(new CredentialsExistenceRule(values))
                .validate();

        return UserController.newInstance().add(values);
    }

    /**
     * Updates an existing {@code Account}.
     *
     * @param websafeUserId      the ID of the entity to be updated
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "users.update",
            path = "users/{userId}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Account update(@Named("userId") final long websafeUserId)
            throws NotFoundException {
        return null;
    }

    /**
     * Deletes the specified {@code Account}.
     *
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "users.remove",
            path = "users",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(user.getUserId()))
                .validate();

        // TODO: 7.07.2016 Implement parentUserKey delete.
    }
}