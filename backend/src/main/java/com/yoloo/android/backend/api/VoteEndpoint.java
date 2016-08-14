package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.users.User;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.controller.VoteController;
import com.yoloo.android.backend.validator.Validator;
import com.yoloo.android.backend.validator.rule.common.AuthenticationRule;
import com.yoloo.android.backend.validator.rule.common.IdValidationRule;
import com.yoloo.android.backend.validator.rule.common.NotFoundRule;

import java.util.logging.Logger;

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
        resource = "votes",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class}
)
public class VoteEndpoint {

    private static final Logger logger =
            Logger.getLogger(VoteEndpoint.class.getName());

    @ApiMethod(
            name = "questions.vote",
            path = "questions/{postId}/votes",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void vote(@Named("postId") final String websafePostId,
                     @Named("dir") final int direction,
                     final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new IdValidationRule(websafePostId))
                .addRule(new AuthenticationRule(user))
                .addRule(new NotFoundRule(websafePostId))
                .validate();

        VoteController.newInstance().vote(websafePostId, direction, user);
    }
}
