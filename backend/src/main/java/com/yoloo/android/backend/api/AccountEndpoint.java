package com.yoloo.android.backend.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.appengine.repackaged.com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.appengine.repackaged.com.google.api.client.http.javanet.NetHttpTransport;
import com.google.appengine.repackaged.com.google.api.client.json.jackson2.JacksonFactory;

import com.yoloo.android.backend.Constants;
import com.yoloo.android.backend.authenticator.GoogleAuthenticator;
import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.service.OfyHelper;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        resource = "accounts",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID},
        authenticators = {GoogleAuthenticator.class}
)
public class AccountEndpoint {

    private static final Logger logger = Logger.getLogger(AccountEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    /**
     * Returns the {@link Account} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "getAccount",
            path = "accounts/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Account get(@Named("id") Long id) throws NotFoundException {
        Account account = OfyHelper.ofy().load().type(Account.class).id(id).now();
        if (account == null) {
            throw new NotFoundException("Could not find Account with ID: " + id);
        }
        return account;
    }

    /**
     * Inserts a new {@code Account}.
     */
    @ApiMethod(
            name = "addAccount",
            path = "accounts",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account add(@Named("token") String token) {
        logger.info("Created Account.");

        return saveProfile(processToken(token));
    }

    /**
     * Updates an existing {@code Account}.
     *
     * @param id      the ID of the entity to be updated
     * @param account the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "updateAccount",
            path = "accounts/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Account update(@Named("id") Long id, Account account) throws NotFoundException {
        checkExists(id);
        OfyHelper.ofy().save().entity(account).now();
        logger.info("Updated Account: " + account);
        return OfyHelper.ofy().load().entity(account).now();
    }

    /**
     * Deletes the specified {@code Account}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "removeAccount",
            path = "accounts/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") Long id) throws NotFoundException {
        checkExists(id);
        OfyHelper.ofy().delete().type(Account.class).id(id).now();
        logger.info("Deleted Account with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listAccount",
            path = "accounts",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> list(@Nullable @Named("cursor") String cursor,
                                            @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Account> query = OfyHelper.ofy().load().type(Account.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Account> queryIterator = query.iterator();
        List<Account> accountList = new ArrayList<>(limit);
        while (queryIterator.hasNext()) {
            accountList.add(queryIterator.next());
        }
        return CollectionResponse.<Account>builder().setItems(accountList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(Long id) throws NotFoundException {
        try {
            OfyHelper.ofy().load().type(Account.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Account with ID: " + id);
        }
    }

    private Payload processToken(String idToken) {
        NetHttpTransport transport = new NetHttpTransport();
        JacksonFactory jacksonFactory = new JacksonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jacksonFactory)
                // TODO: 1.06.2016 add audience
                .setAudience(Collections.singletonList(Constants.WEB_CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                return googleIdToken.getPayload();
            }
        } catch (GeneralSecurityException | IOException e) {
            logger.info(e.getMessage());
        }

        return null;
    }

    private Account saveProfile(Payload payload) {
        Account account = new Account.Builder()
                .setUsername((String) payload.get("name"))
                .setRealname((String) payload.get("given_name"))
                .setEmail(payload.getEmail())
                .setPictureUrl((String) payload.get("picture"))
                .build();

        OfyHelper.ofy().save().entity(account).now();

        return account;
    }
}