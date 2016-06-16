package com.yoloo.android.backend.modal.location;

/**
 * LocationInfo class extending the Location with additional information.
 */
public class LocationInfo extends Location {

    /**
     * The distance to this place from the current working position.
     */
    private double distanceInKilometers;

    /**
     * Returns the distance to this place from the current working position.
     *
     * @return The distance to this place.
     */
    public final double getDistanceInKilometers() {
        return distanceInKilometers;
    }

    /**
     * Sets the distance to this place from the current working position.
     *
     * @param distanceInKilometers the distance to this place.
     */
    public final void setDistanceInKilometers(final double distanceInKilometers) {
        this.distanceInKilometers = distanceInKilometers;
    }
}
