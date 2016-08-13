package com.yoloo.android.backend.util;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.feed.post.Post;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.location.LocationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.api.utils.SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.environment;

/**
 * Helper class for geo-proximity related management of Locations.
 */
public final class LocationHelper {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(LocationHelper.class.getName());

    /**
     * The datastore index containing the places that we will use to search.
     */
    private static final String INDEX_NAME = "Location";

    /**
     * The double precision to use for comparisons.
     */
    private static final double EPSILON = 0.0001;

    /**
     * The number of meters in a kilometer.
     */
    private static final int METERS_IN_KILOMETER = 1000;

    /**
     * The radius of the earth, in kilometers.
     */
    private static final double EARTH_RADIUS = 6378.1;

    /**
     * A fake distance used in the dev environment.
     */
    private static final int FAKE_DISTANCE_FOR_DEV = 5;

    /**
     * Default constructor, never called.
     */
    private LocationHelper() {
    }

    /**
     * Returns the Places index in the datastore.
     *
     * @return The index to use to search places in the datastore.
     */
    public static Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME)
                .build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    /**
     * Builds a new Place document to insert in the Places index.
     *
     * @param placeId      the identifier of the place in the database.
     * @param placeName    the name of the place.
     * @param placeAddress the address of the place.
     * @param location     the GPS location of the place, as a GeoPt.
     * @return the Place document created.
     */
    public static Document buildDocument(
            final Long placeId, final String placeName,
            final String placeAddress, final GeoPt location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),
                location.getLongitude());

        Document.Builder builder = Document.newBuilder()
                .addField(Field.newBuilder().setName("id")
                        .setText(placeId.toString()))
                .addField(Field.newBuilder().setName("name").setText(placeName))
                .addField(Field.newBuilder().setName("address")
                        .setText(placeAddress))
                .addField(Field.newBuilder().setName("place_location")
                        .setGeoPoint(geoPoint));

        // geo-location doesn't work under dev_server, so let's add another
        // field to use for retrieving documents
        if (environment.value() == Development) {
            builder.addField(Field.newBuilder().setName("value").setNumber(1));
        }

        return builder.build();
    }

    /**
     * Returns the nearest places to the location of the parentUserKey.
     *
     * @param location         the location of the parentUserKey.
     * @param distanceInMeters the maximum distance to the parentUserKey.
     * @param resultCount      the maximum number of places returned.
     * @return List of up to resultCount places in the datastore ordered by
     * the distance to the location parameter and less than
     * distanceInMeters meters to the location parameter.
     */
    public static List<LocationInfo> getPlaces(final GeoPt location,
                                               final long distanceInMeters,
                                               final int resultCount) {

        // Optional: use memcache

        String geoPoint = "geopoint(" + location.getLatitude() + ", " + location
                .getLongitude()
                + ")";
        String locExpr = "distance(place_location, " + geoPoint + ")";

        // Build the SortOptions with 2 sort keys
        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression(locExpr)
                        .setDirection(SortExpression.SortDirection.ASCENDING)
                        .setDefaultValueNumeric(distanceInMeters + 1))
                .setLimit(resultCount)
                .build();
        // Build the QueryOptions
        QueryOptions options = QueryOptions.newBuilder()
                .setSortOptions(sortOptions)
                .build();
        // Query string
        String searchQuery = "distance(place_location, " + geoPoint + ") < "
                + distanceInMeters;

        Query query = Query.newBuilder().setOptions(options).build(searchQuery);

        Results<ScoredDocument> results = getIndex().search(query);

        if (results.getNumberFound() == 0) {
            // geo-location doesn't work under dev_server
            if (environment.value() == Development) {
                // return all documents
                results = getIndex().search("value > 0");
            }
        }

        List<LocationInfo> locations = new ArrayList<>();

        for (ScoredDocument document : results) {
            if (locations.size() >= resultCount) {
                break;
            }

            GeoPoint p = document.getOnlyField("place_location").getGeoPoint();

            LocationInfo place = new LocationInfo();

            /*place.setPlaceId(Long.valueOf(document.getOnlyField("id")
                    .getText()));*/
            place.setName(document.getOnlyField("name").getText());
            //place.setAddress(document.getOnlyField("address").getText());

            place.setGeoPt(new GeoPt((float) p.getLatitude(),
                    (float) p.getLongitude()));

            // GeoPoints are not implemented on dev server and latitude and
            // longitude are set to zero
            // But since those are doubles let's play safe
            // and use double comparison with epsilon set to EPSILON
            if (Math.abs(p.getLatitude()) <= EPSILON
                    && Math.abs(p.getLongitude()) <= EPSILON) {
                // set a fake distance of 5+ km
                place.setDistanceInKilometers(FAKE_DISTANCE_FOR_DEV + locations
                        .size());
            } else {
                double distance = distanceInMeters / METERS_IN_KILOMETER;
                try {
                    distance = getDistanceInKm(
                            p.getLatitude(), p.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude());
                } catch (Exception e) {
                    LOG.warning("Exception when calculating a distance: " + e
                            .getMessage());
                }

                place.setDistanceInKilometers(distance);
            }

            locations.add(place);
        }
        return locations;
    }

    /**
     * Computes the geodesic distance between two GPS coordinates.
     *
     * @param latitude1  the latitude of the first point.
     * @param longitude1 the longitude of the first point.
     * @param latitude2  the latitude of the second point.
     * @param longitude2 the longitude of the second point.
     * @return the geodesic distance between the two points, in kilometers.
     */
    private static double getDistanceInKm(
            final double latitude1, final double longitude1,
            final double latitude2, final double longitude2) {

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double long1 = Math.toRadians(longitude1);
        double long2 = Math.toRadians(longitude2);

        return EARTH_RADIUS * Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
                        * Math.cos(lat2) * Math.cos(Math.abs(long1 - long2)));
    }

    public static GeoPt createLocationFromString(String latLng) {
        final List<String> coords = StringUtil.splitValueByToken(latLng, ",");

        return new GeoPt(Float.valueOf(coords.get(0)), Float.valueOf(coords.get(1)));
    }

    public static List<Location> getLocationList(String locationArgs,
                                                 Key<? extends Post> postKey) {
        return ImmutableList.copyOf(getLocations(locationArgs, postKey));
    }

    public static Set<Location> getLocationSet(String locationArgs,
                                               Key<? extends Post> postKey) {
        return ImmutableSet.copyOf(getLocations(locationArgs, postKey));
    }

    private static List<Location> getLocations(String locationArgs,
                                        Key<? extends Post> postKey) {
        List<String> locationParts = StringUtil.splitValueByToken(locationArgs, ";");
        List<Location> locations = new ArrayList<>(3);
        for (String part : locationParts) {
            List<String> block = StringUtil.splitValueByToken(part, ":");

            Location location = Location.builder()
                    .setPostKey(postKey)
                    .setName(block.get(0))
                    .setGeoPt(LocationHelper.createLocationFromString(block.get(1)))
                    .build();

            locations.add(location);
        }

        return locations;
    }
}
