package com.yoloo.android.backend.model.feed.post;

import com.google.api.client.util.Strings;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;
import com.google.common.collect.Sets;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.android.backend.model.comment.Commentable;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@JsonPropertyOrder({"id", "ownerId", "profileImageUrl", "username", "type", "content",
        "hashtags", "locations", "commented", "liked", "likes", "comments",
        "reports", "reportedBy", "createdAt", "updatedAt"})
public class TimelinePost extends Post implements Commentable {

    @Index
    private Set<String> hashtags;

    private Set<Key<Account>> reportedByKeys;

    private Link videoUrl;

    private Set<Location> locations;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    @Ignore
    private boolean isLiked = false;

    @Ignore
    private boolean isCommented = false;

    @Ignore
    private long likes = 0L;

    @Ignore
    private long comments = 0L;

    @Ignore
    private int reports = 0;

    // Methods

    private TimelinePost() {
    }

    private TimelinePost(Builder<?> builder) {
        super(builder);
        this.videoUrl = builder.videoUrl;
        this.hashtags = builder.hashtags;
        this.locations = builder.locations;
        this.updatedAt = new Date();
    }

    public static Builder<?> builder() {
        return new Builder<TimelinePost>() {
            @Override
            public TimelinePost build() {
                return new TimelinePost(this);
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

    public Link getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = Strings.isNullOrEmpty(videoUrl) ? null : new Link(videoUrl);
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean commented) {
        isCommented = commented;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
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

    public static abstract class Builder<T extends TimelinePost> extends Post.Builder<T> {
        private Link videoUrl;
        private Set<String> hashtags;
        private Set<Location> locations;

        public Builder<T> setVideoUrl(String videoUrl) {
            this.videoUrl = Strings.isNullOrEmpty(videoUrl) ? null : new Link(videoUrl);
            return this;
        }

        public Builder<T> setHashtags(List<String> hashtags) {
            this.hashtags = hashtags == null
                    ? Collections.<String>emptySet()
                    : Sets.newHashSet(hashtags);
            return this;
        }

        public Builder<T> setLocations(List<Location> locations) {
            this.locations = locations == null
                    ? Collections.<Location>emptySet()
                    : Sets.newHashSet(locations);
            return this;
        }

        public Builder<T> setLocations(Set<Location> locations) {
            this.locations = locations == null
                    ? Collections.<Location>emptySet()
                    : locations;
            return this;
        }
    }
}
