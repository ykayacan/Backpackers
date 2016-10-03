package com.backpackers.android.backend.model.feed.post;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;
import com.google.common.collect.Sets;

import com.backpackers.android.backend.model.like.Likeable;
import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@JsonPropertyOrder({"id", "ownerId", "profileImageUrl", "username", "type", "content",
        "hashtags", "location", "commented", "liked", "likes", "comments",
        "reports", "reportedBy", "createdAt", "updatedAt"})
public class NormalPost extends AbstractPost implements Likeable {

    @Index
    private Set<String> hashtags;

    private Set<Key<Account>> reportedByKeys;

    private Location location;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    // Extra fields

    @Ignore
    private boolean isLiked = false;

    @Ignore
    private long likes = 0L;

    @Ignore
    private boolean isCommented = false;

    @Ignore
    private long comments = 0L;

    @Ignore
    private int reports = 0;

    // Methods

    private NormalPost() {
    }

    private NormalPost(Builder<?> builder) {
        super(builder);
        this.hashtags = builder.hashtags;
        this.location = builder.location;
        this.updatedAt = new Date();
    }

    public static Builder<?> builder() {
        return new Builder<NormalPost>() {
            @Override
            public NormalPost build() {
                return new NormalPost(this);
            }
        };
    }

    public Set<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(Set<String> hashtags) {
        this.hashtags = hashtags;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<Key<Account>> getReportedByKeys() {
        return reportedByKeys;
    }

    public void setReportedByKeys(Set<Key<Account>> reportedByKeys) {
        this.reportedByKeys = reportedByKeys;
    }

    @JsonProperty("reportedBy")
    public List<String> getReportedByIds() {
        if (reportedByKeys == null) {
            return null;
        }

        List<String> reportIds = new ArrayList<>();
        for (Key<Account> key : reportedByKeys) {
            reportIds.add(key.toWebSafeString());
        }
        return reportIds;
    }

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean commented) {
        isCommented = commented;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getComments() {
        return comments;
    }

    public void setComments(long comments) {
        this.comments = comments;
    }

    public int getReports() {
        return reports;
    }

    public void setReports(int reports) {
        this.reports = reports;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    @Override
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<? extends Likeable> getLikeableKey() {
        return Key.create(parentUserKey, NormalPost.class, id);
    }

    @Override
    public void setEntityLiked(boolean liked) {
        this.setLiked(liked);
    }

    @Override
    public void setEntityLikes(long count) {
        this.setLikes(count);
    }

    public static abstract class Builder<T extends NormalPost> extends AbstractPost.Builder<T> {
        private Set<String> hashtags;
        private Location location;

        public Builder<T> setHashtags(List<String> hashtags) {
            this.hashtags = hashtags == null
                    ? Collections.<String>emptySet()
                    : Sets.newHashSet(hashtags);
            return this;
        }

        public Builder<T> setLocation(Location location) {
            if (location != null) {
                this.location = location;
            } else {
                this.location = null;
            }
            return this;
        }
    }
}
