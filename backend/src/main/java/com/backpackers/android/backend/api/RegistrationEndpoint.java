/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.backpackers.android.backend.api;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;

import com.backpackers.android.backend.authenticator.GoogleAuthenticator;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.backpackers.android.backend.validator.Validator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.backpackers.android.backend.Constants;
import com.backpackers.android.backend.authenticator.FacebookAuthenticator;
import com.backpackers.android.backend.authenticator.YolooAuthenticator;
import com.backpackers.android.backend.model.RegistrationRecord;
import com.backpackers.android.backend.validator.rule.common.AuthenticationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

/**
 * A registration endpoint class we are exposing for a device's GCM registration id on the backend
 *
 * For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 *
 * NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.
 */
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
        resource = "registrations",
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
public class RegistrationEndpoint {

    private static final Logger LOGGER =
            Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Registers a device to the backend.
     *
     * @param regId The Google Cloud Messaging registration id to add.
     *              parentUserKey is unauthenticated
     */
    @ApiMethod(
            name = "registrations.register",
            path = "registrations",
            httpMethod = ApiMethod.HttpMethod.POST)
    public void registerDevice(@Named("regId") String regId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        RegistrationRecord record = OfyHelper.ofy().load().type(RegistrationRecord.class)
                .ancestor(Key.create(user.getUserId()))
                .first().now();

        if (record == null) {
            record = new RegistrationRecord(Key.<Account>create(user.getUserId()), regId);
        } else {
            record.setRegId(regId);
        }

        OfyHelper.ofy().save().entity(record).now();
    }

    /**
     * Unregisters a device from the backend.
     *
     * @param regId The Google Cloud Messaging registration Id to remove
     *              parentUserKey is unauthorized
     */
    @ApiMethod(
            name = "registrations.unregister",
            path = "registrations",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void unregisterDevice(@Named("regId") String regId, final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        Key<RegistrationRecord> recordKey = findRecordKey(regId);

        if (recordKey == null) {
            LOGGER.info("Device " + regId + " not registered, skipping unregister");
            return;
        }

        OfyHelper.ofy().delete().entity(recordKey).now();
    }

    /**
     * Return a collection of registered devices
     *
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(
            name = "registrations.list",
            path = "registrations",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<RegistrationRecord> listDevices(@Nullable @Named("cursor") final String cursor,
                                                              @Nullable @Named("limit") Integer limit,
                                                              final User user)
            throws ServiceException {

        Validator.builder()
                .addRule(new AuthenticationRule(user))
                .validate();

        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        Query<RegistrationRecord> query = OfyHelper.ofy().load().type(RegistrationRecord.class);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<RegistrationRecord> queryIterator = query.iterator();

        final List<RegistrationRecord> records = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            records.add(queryIterator.next());
        }

        return CollectionResponse.<RegistrationRecord>builder()
                .setItems(records)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }

    /**
     * Searches an entity by ID.
     *
     * @param regId the registration ID to search
     * @return the Registration associated to regId
     */
    private Key<RegistrationRecord> findRecordKey(String regId) {
        return OfyHelper.ofy().load().type(RegistrationRecord.class)
                .filter("regId", regId).keys().first().now();
    }

}
