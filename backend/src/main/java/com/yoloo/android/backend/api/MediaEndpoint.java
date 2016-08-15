package com.yoloo.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.FacebookAuthenticator;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.authenticator.YolooAuthenticator;
import com.yoloo.android.backend.model.media.MediaToken;
import com.yoloo.android.backend.response.WrappedString;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

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
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {
                GoogleAuthenticator.class,
                FacebookAuthenticator.class,
                YolooAuthenticator.class
        }
)
public class MediaEndpoint {

    @ApiMethod(
            name = "medias.token",
            path = "medias/",
            httpMethod = ApiMethod.HttpMethod.GET)
    public WrappedString getToken() throws ServiceException {

        /*Validator.builder()
                .addRule(new AuthenticationRule(parentUserKey))
                .validate();*/

        final MediaToken token = new MediaToken();

        final Key<MediaToken> tokenKey = ofy().save().entity(token).now();

        return new WrappedString(tokenKey.toWebSafeString());
    }
}
