package com.yoloo.android.backend.model.feed.post;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonProperty;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonPropertyOrder;
import com.google.common.collect.Sets;

import com.googlecode.objectify.Key;
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
        "hashtags", "locations", "commented", "status", "ups", "downs", "comments",
        "reports", "reportedBy", "awardedByWebsafeId", "awardRep", "locked", "accepted",
        "createdAt", "updatedAt"})
public class ForumPost extends AbstractPost implements Commentable {

    private boolean isLocked = false;

    @Index
    private Set<String> hashtags;

    private Set<Key<Account>> reportedByKeys = new HashSet<>();

    private String videoUrl;

    private boolean isAccepted = false;

    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private String awardedByWebsafeId;

    private int awardRep = 0;

    private Set<Location> locations;

    @Index(IfNotDefault.class)
    private Date updatedAt;

    // Extra fields

    @Ignore
    private boolean isCommented = false;

    @Ignore
    private Vote.Status status = Vote.Status.DEFAULT;

    @Ignore
    private long ups = 0L;

    @Ignore
    private long downs = 0L;

    // Ignore the parameter. Enable it in next feature release.
    @Ignore
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private long views = 0L;

    @Ignore
    private long comments = 0L;

    @Ignore
    private int reports = 0;

    // Methods

    private ForumPost() {
    }

    private ForumPost(Builder<?> builder) {
        super(builder);
        this.isLocked = builder.isLocked;
        this.videoUrl = builder.videoUrl;
        this.hashtags = builder.hashtags;
        this.locations = builder.locations;
        this.awardedByWebsafeId = builder.awardedByWebsafeId;
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
    public String getAwardedByWebsafeId() {
        return awardedByWebsafeId;
    }

    public void setAwardedByWebsafeId(Key<Account> awardedByKey) {
        this.awardedByWebsafeId = awardedByKey.toWebSafeString();
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

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean commented) {
        isCommented = commented;
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

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
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

    public static abstract class Builder<T extends ForumPost> extends AbstractPost.Builder<T> {
        private boolean isLocked = false;
        private String videoUrl;
        private Set<String> hashtags;
        private Set<Location> locations;
        private String awardedByWebsafeId;
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

        public Builder<T> setAward(Key<Account> awardedBy, Integer awardRep) {
            if (awardRep != null && awardRep != 0) {
                this.awardedByWebsafeId = awardedBy.toWebSafeString();
                this.awardRep = awardRep;
            } else {
                this.awardRep = 0;
                this.awardedByWebsafeId = null;
            }
            return this;
        }
    }
}
