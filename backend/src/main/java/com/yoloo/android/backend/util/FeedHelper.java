package com.yoloo.android.backend.util;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import com.yoloo.android.backend.modal.Account;
import com.yoloo.android.backend.modal.Comment;
import com.yoloo.android.backend.modal.Feed;
import com.yoloo.android.backend.modal.Hashtag;
import com.yoloo.android.backend.modal.location.Location;
import com.yoloo.android.backend.request.FeedRequest;
import com.yoloo.android.backend.service.OfyHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.QueryKeys;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class FeedHelper {

    private static final Logger logger = Logger.getLogger(FeedHelper.class.getName());

    private static void addLocation(final Key<Feed> feedKey, final FeedRequest request) {
        Location location = new Location();
        location.setFeedRef(feedKey);
        location.setName(request.getLocation());
        location.setGeoPt(request.getLatitude(), request.getLongitude());

        OfyHelper.ofy().save().entity(location);

        logger.info("Created Location with ID: " + location.getId());
    }

    private static void addHashTags(final Key<Feed> feedKey, final FeedRequest request) {
        for (String hashTagString : request.getHashtags()) {
            // Create a new hashTag from string.
            Hashtag hashtag = new Hashtag();
            hashtag.setFeedRef(feedKey);
            hashtag.setName(hashTagString);

            // Save hashTag.
            OfyHelper.ofy().save().entity(hashtag);

            logger.info("Created HashTag with ID: " + hashtag.getId());
        }
    }

    private static void addImages(final Key<Feed> feedKey, final FeedRequest request) {
        // TODO: 2.06.2016 Implement image saving.
        final File[] files = request.getFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    byte[] imageBytes = Files.readAllBytes(file.toPath());

                    // Get an instance of the imagesService we can use to transform images.
                    ImagesService imagesService = ImagesServiceFactory.getImagesService();

                    // Make an image directly from a byte array, and transform it.
                    Image image = ImagesServiceFactory.makeImage(imageBytes);
                    Transform resize = ImagesServiceFactory.makeResize(75, 75);
                    Image resizedImage = imagesService.applyTransform(resize, image);

                    GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
                            .initialRetryDelayMillis(10)
                            .retryMaxAttempts(10)
                            .totalRetryPeriodMillis(15000)
                            .build());

                    final String bucket = "YOUR-BUCKETNAME-HERE";

                    // Write the transformed image back to a Cloud Storage object.
                    gcsService.createOrReplace(
                            new GcsFilename(bucket, "resizedImage.jpeg"),
                            new GcsFileOptions.Builder().mimeType("image/jpeg").build(),
                            ByteBuffer.wrap(resizedImage.getImageData()));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deleteHashTags(final Key<Feed> feedKey) {
        QueryKeys<Hashtag> hashTagKeys = OfyHelper.ofy().load().type(Hashtag.class)
                .filter("feedRef =", feedKey).keys();

        OfyHelper.ofy().delete().keys(hashTagKeys);
    }

    private static void deleteLocation(final Key<Feed> feedKey) {
        QueryKeys<Location> locationKeys = OfyHelper.ofy().load().type(Location.class)
                .filter("feedRef =", feedKey).keys();

        OfyHelper.ofy().delete().keys(locationKeys);
    }

    private static void deleteImages(final Key<Feed> feedKey) {
        QueryKeys<Hashtag> hashTagKeys = OfyHelper.ofy().load().type(Hashtag.class)
                .filter("feedRef =", feedKey).keys();
    }

    public static List<String> fetchHashtags(final Key<Feed> feedKey) {
        List<Hashtag> hashtags = OfyHelper.ofy().load().type(Hashtag.class).filter("feedRef =", feedKey).list();
        List<String> hashTagStrings = new ArrayList<>(hashtags.size());
        for (Hashtag hashtag : hashtags) {
            hashTagStrings.add(hashtag.getName());
        }

        return hashTagStrings;
    }

    public static List<String> fetchLocationNames(final Key<Feed> feedKey) {
        List<Location> locations = OfyHelper.ofy().load().type(Location.class).filter("feedRef =", feedKey).list();
        List<String> locationNames = new ArrayList<>(locations.size());
        for (Location location : locations) {
            locationNames.add(location.getName());
        }

        return locationNames;
    }

    /**
     * Creates a new Feed with given parameters.
     *
     * @param request the FeedRequest
     * @return the Feed
     */
    public static Feed createFeed(final Key<Account> accountKey, final FeedRequest request) {
        final int type = (request.getFiles() != null
                && request.getFiles().length > 0) ? 1 : 0;

        // TODO: 10.06.2016 think about async way.

        Feed feed = new Feed.Builder()
                .setAccount(accountKey)
                .setTitle(request.getTitle())
                .setContent(request.getMessage())
                .setType(type)
                .hashtags(Arrays.asList(request.getHashtags()))
                .locations(Collections.singletonList(request.getLocation()))
                .build();

        // Save feed.
        Key<Feed> feedKey = OfyHelper.ofy().save().entity(feed).now();

        addLocation(feedKey, request);
        addHashTags(feedKey, request);
        addImages(feedKey, request);

        return feed;
    }

    public static void removeFeed(final long id) {
        Key<Feed> feedKey = Key.create(Feed.class, id);

        deleteLocation(feedKey);
        deleteHashTags(feedKey);
        deleteImages(feedKey);

        LikeHelper.deleteChildLike(Feed.class, feedKey);
        LikeHelper.deleteChildLike(Comment.class, feedKey);

        OfyHelper.ofy().delete().type(Feed.class).id(id);
    }
}
