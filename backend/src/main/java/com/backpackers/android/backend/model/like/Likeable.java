package com.backpackers.android.backend.model.like;

import com.googlecode.objectify.Key;

public interface Likeable {

    Key<? extends Likeable> getLikeableKey();

    void setEntityLiked(boolean liked);

    void setEntityLikes(long count);
}
