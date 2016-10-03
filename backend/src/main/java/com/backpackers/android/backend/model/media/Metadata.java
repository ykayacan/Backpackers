package com.backpackers.android.backend.model.media;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class Metadata {

    private String bucketName;
    private String objectName;

    private String mimeType;
    private String cacheControl;

    private String etag;
    private long length;
    private Date lastModified;

    private Metadata() {
    }

    public Metadata(GcsFilename filename, GcsFileOptions options, String etag, long length, Date lastModified) {
        Preconditions.checkArgument(length >= 0, "Length must be positive");
        this.bucketName = checkNotNull(filename, "Null filename").getBucketName();
        this.objectName = checkNotNull(filename, "Null filename").getObjectName();

        this.mimeType = checkNotNull(options, "Null options").getMimeType();
        this.cacheControl = checkNotNull(options, "Null options").getCacheControl();

        this.etag = etag;
        this.length = length;
        this.lastModified = lastModified;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }


    public String getMimeType() {
        return mimeType;
    }

    public String getCacheControl() {
        return cacheControl;
    }


    public String getEtag() {
        return etag;
    }

    public long getLength() {
        return length;
    }

    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "bucketName='" + bucketName + '\'' +
                ", objectName='" + objectName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", cacheControl='" + cacheControl + '\'' +
                ", etag='" + etag + '\'' +
                ", length=" + length +
                ", lastModified=" + lastModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metadata)) return false;
        Metadata metadata = (Metadata) o;
        return length == metadata.length &&
                Objects.equal(bucketName, metadata.bucketName) &&
                Objects.equal(objectName, metadata.objectName) &&
                Objects.equal(mimeType, metadata.mimeType) &&
                Objects.equal(cacheControl, metadata.cacheControl) &&
                Objects.equal(etag, metadata.etag) &&
                Objects.equal(lastModified, metadata.lastModified);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bucketName, objectName, mimeType, cacheControl, etag, length, lastModified);
    }
}
