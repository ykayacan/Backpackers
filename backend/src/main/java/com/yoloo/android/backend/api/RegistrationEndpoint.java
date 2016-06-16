/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package com.yoloo.android.backend.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.modal.RegistrationRecord;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

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
        audiences = {Constants.AUDIENCE_ID}
)
public class RegistrationEndpoint {

    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    /**
     * Registers a device to the backend.
     *
     * @param regId The Google Cloud Messaging registration id to add.
     *              user is unauthenticated
     */
    @ApiMethod(httpMethod = "POST")
    public void registerDevice(@Named("regId") String regId) {
        if (findRecord(regId) != null) {
            log.info("Device " + regId + " already registered, skipping register");
            return;
        }
        RegistrationRecord record = new RegistrationRecord();
        record.setRegId(regId);
        ofy().save().entity(record).now();
    }

    /**
     * Unregisters a device from the backend.
     *
     * @param regId The Google Cloud Messaging registration Id to remove     *
     *              user is unauthorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public void unregisterDevice(@Named("regId") String regId) {
        RegistrationRecord record = findRecord(regId);
        if (record == null) {
            log.info("Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    /**
     * Return a collection of registered devices
     *
     * @param count The number of devices to list
     * @return a list of Google Cloud Messaging registration Ids
     */
    @ApiMethod(httpMethod = "GET")
    public CollectionResponse<RegistrationRecord> listDevices(@Named("count") int count) {
        List<RegistrationRecord> records = ofy().load()
                .type(RegistrationRecord.class).limit(count)
                .list();
        return CollectionResponse.<RegistrationRecord>builder()
                .setItems(records).build();
    }

    /**
     * Searches an entity by ID.
     *
     * @param regId the registration ID to search
     * @return the Registration associated to regId
     */
    private RegistrationRecord findRecord(String regId) {
        return ofy().load().type(RegistrationRecord.class)
                .filter("regId", regId).first().now();
    }

}
