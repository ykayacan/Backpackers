package com.backpackers.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.authenticator.GoogleAuthenticator;
import com.backpackers.android.backend.controller.MediaController;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.validator.Validator;
import com.backpackers.android.backend.validator.rule.common.IdValidationRule;
import com.backpackers.android.backend.validator.rule.common.NotFoundRule;
import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.authenticator.FacebookAuthenticator;
import com.backpackers.android.backend.authenticator.YolooAuthenticator;
import com.backpackers.android.backend.validator.rule.common.AuthenticationRule;

import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

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
        resource = "medias",
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
public class MediaEndpoint {

    private static final Logger logger =
            Logger.getLogger(MediaEndpoint.class.getName());

    @ApiMethod(
            name = "users.medias",
            path = "users/{userId}/medias",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Media> listMedias(@Named("userId") final String websafeUserId,
                                                @Nullable @Named("cursor") String cursor,
                                                @Nullable @Named("limit") Integer limit,
                                                final User user)
            throws ServiceException {
        Validator.builder()
                .addRule(new IdValidationRule(websafeUserId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeUserId))
                .validate();

        return MediaController.newInstance().listMedias(cursor, limit, websafeUserId);
    }

    @ApiMethod(
            name = "users.me.medias",
            path = "users/me/medias",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Media> listSelfMedias(@Nullable @Named("cursor") String cursor,
                                                    @Nullable @Named("limit") Integer limit,
                                                    final User user) throws ServiceException {
        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return MediaController.newInstance().listMedias(cursor, limit, user.getUserId());
    }
}
