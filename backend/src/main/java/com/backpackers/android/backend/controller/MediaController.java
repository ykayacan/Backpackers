package com.backpackers.android.backend.controller;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;

import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.service.OfyHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MediaController {

    /**
     * Maximum number of feeds to return.
     */
    private static final int DEFAULT_LIST_LIMIT = 20;

    private static final Logger logger =
            Logger.getLogger(MediaController.class.getName());

    /**
     * New instance user controller.
     *
     * @return the user controller
     */
    public static MediaController newInstance() {
        return new MediaController();
    }

    public CollectionResponse<Media> listMedias(final String cursor,
                                                Integer limit,
                                                final String websafeUserId) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;

        final Key<Account> userKey = Key.create(websafeUserId);

        Query<Media> query = OfyHelper.ofy().load().type(Media.class).ancestor(userKey);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }

        query = query.limit(limit);

        final QueryResultIterator<Media> queryIterator = query.iterator();

        final List<Media> medias = new ArrayList<>(DEFAULT_LIST_LIMIT);
        while (queryIterator.hasNext()) {
            medias.add(queryIterator.next());
        }

        return CollectionResponse.<Media>builder()
                .setItems(medias)
                .setNextPageToken(queryIterator.getCursor().toWebSafeString())
                .build();
    }
}
