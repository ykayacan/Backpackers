package com.backpackers.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.authenticator.FacebookAuthenticator;
import com.backpackers.android.backend.authenticator.GoogleAuthenticator;
import com.backpackers.android.backend.authenticator.YolooAuthenticator;
import com.backpackers.android.backend.controller.NotificationController;
import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.validator.Validator;
import com.backpackers.android.backend.validator.rule.common.AuthenticationRule;
import com.backpackers.android.backend.validator.rule.common.NotFoundRule;

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
        resource = "messages",
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
public class NotificationEndpoint {

    /**
     * Inserts a new {@code Notification}.
     */
    @ApiMethod(
            name = "notifications.add",
            path = "notifications",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void add(@Named("receiverId") final String websafeReceiverId,
                    @Named("action") final Notification.Action action,
                    @Nullable @Named("content") final String content,
                    @Nullable @Named("postId") final String websafePostId,
                    @Nullable @Named("commentId") final String websafeCommentId,
                    @Nullable @Named("photoUrl") final String photoUrl,
                    final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        NotificationController.newInstance().add(websafeReceiverId, action, content,
                websafePostId, websafeCommentId, photoUrl, user);
    }

    /**
     * Removes a new {@code Notification}.
     */
    @ApiMethod(
            name = "notifications.remove",
            path = "notifications/{notificationId}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("notificationId") final String websafeNotificationId,
                       final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafeNotificationId))
                .validate();

        NotificationController.newInstance().remove(websafeNotificationId, user);
    }

    /**
     * Removes a new {@code Notification}.
     */
    @ApiMethod(
            name = "notifications.list",
            path = "notifications",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Notification> list(@Nullable @Named("cursor") final String cursor,
                                                 @Nullable @Named("limit") Integer limit,
                                                 final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        return NotificationController.newInstance().list(cursor, limit, user);
    }
}
