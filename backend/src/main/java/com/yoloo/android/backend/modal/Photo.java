package com.yoloo.android.backend.modal;

import com.google.appengine.api.datastore.Link;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Cache
public class Photo {

    /**
     * Small square 75x75.
     */
    public static final int PHOTO_S = 0;
    /**
     * Large square 150x150.
     */
    public static final int PHOTO_Q = 1;
    /**
     * Thumbnail, 100 on the longest side.
     */
    public static final int PHOTO_T = 2;
    /**
     * Small, 240 on the longest side.
     */
    public static final int PHOTO_M = 3;
    /**
     * Small, 320 on the longest side.
     */
    public static final int PHOTO_N = 4;
    /**
     * Medium, 500 on the longest side.
     */
    public static final int PHOTO_D = 5;

    @Id
    private Long id;

    @Getter
    @Setter
    private int type;

    @Getter
    @Setter
    private int width;

    @Getter
    @Setter
    private int height;

    @Getter
    @Setter
    private Link url;
}
