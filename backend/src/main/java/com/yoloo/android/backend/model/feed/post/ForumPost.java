package com.yoloo.android.backend.model.feed.post;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;
import com.google.common.collect.Sets;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.condition.IfNotDefault;
import com.yoloo.android.backend.model.comment.Commentable;
import com.yoloo.android.backend.model.location.Location;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.model.vote.Vote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Cache
@JsonPropertyOrder({"id", "ownerId", "profileImageUrl", "username", "type", "content",
        "hashtags", "locations", "status", "ups", "downs", "commentCount", "reportCount",
        "reportedBy", "awardedBy", "awardRep", "locked", "accepted", "createdAt", "updatedAt"})
public class ForumPost extends Post implements Commentable {

    private boolean isLocked = false;

    @Index
    private Set<String> hashtags;

    private Set<Key<Account>> reportedByKeys = new HashSet<>();

    private String videoUrl;

    private boolean isAccepted = false;

    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Ref<Account> awardedBy;

    private int awardRep = 0;

    private Set<Location> locations;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    @Ignore
    private Vote.Status status = Vote.Status.DEFAULT;

    @Ignore
    private long ups = 0L;

    @Ignore
    private long downs = 0L;

    // Ignore the parameter. Enable it in next feature release.
    @Ignore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long viewCount = 0L;

    @Ignore
    private long commentCount = 0L;

    @Ignore
    private int reportCount = 0;

    // Methods

    private ForumPost() {
    }

    private ForumPost(Builder<?> builder) {
        super(builder);
        this.isLocked = builder.isLocked;
        this.videoUrl = builder.videoUrl;
        this.hashtags = builder.hashtags;
        this.locations = builder.locations;
        this.awardedBy = builder.awardedBy;
        this.awardRep = builder.awardRep;
        this.updatedAt = new Date();
    }

    public static Builder<?> builder() {
        return new Builder<ForumPost>() {
            @Override
            public ForumPost build() {
                return new ForumPost(this);
            }
        };
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    @JsonProperty("awardedBy")
    public String getAwardedBy() {
        return awardedBy.getKey().toWebSafeString();
    }

    public void setAwardedBy(Key<Account> awardedByKey) {
        this.awardedBy = Ref.create(awardedByKey);
    }

    public int getAwardRep() {
        return awardRep;
    }

    public void setAwardRep(int awardRep) {
        this.awardRep = awardRep;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Vote.Status getStatus() {
        return status;
    }

    public void setStatus(Vote.Status status) {
        this.status = status;
    }

    public long getUps() {
        return ups;
    }

    public void setUps(long ups) {
        this.ups = ups;
    }

    public long getDowns() {
        return downs;
    }

    public void setDowns(long downs) {
        this.downs = downs;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public int getReportCount() {
        return reportCount;
    }

    public void setReportCount(int reportCount) {
        this.reportCount = reportCount;
    }

    public static abstract class Builder<T extends ForumPost> extends Post.Builder<T> {
        private boolean isLocked = false;
        private String videoUrl;
        private Set<String> hashtags;
        private Set<Location> locations;
        private Ref<Account> awardedBy;
        private Integer awardRep;

        public Builder<T> setLocked(Boolean locked) {
            isLocked = locked == null ? false : locked;
            return this;
        }

        public Builder<T> setVideoUrl(String videoUrl) {
            this.videoUrl = videoUrl;
            return this;
        }

        public Builder<T> setHashtags(List<String> hashtags) {
            this.hashtags = hashtags == null ?
                    Collections.<String>emptySet() : Sets.newHashSet(hashtags);
            return this;
        }

        public Builder<T> setLocations(List<Location> locations) {
            this.locations = locations == null ?
                    Collections.<Location>emptySet() : Sets.newHashSet(locations);
            return this;
        }

        public Builder<T> setAward(Key<Account> awardedBy, Integer awardRep) {
            if (awardRep != null) {
                this.awardedBy = Ref.create(awardedBy);
                this.awardRep = awardRep;
            }
            return this;
        }
    }
}
