package com.backpackers.android.backend.mapper;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;

import com.backpackers.android.backend.model.media.Metadata;

public class MetadataMapper implements Mapper<GcsFileMetadata, Metadata> {

    @Override
    public Metadata map(GcsFileMetadata gcsFileMetadata) {
        return new Metadata(
                gcsFileMetadata.getFilename(),
                gcsFileMetadata.getOptions(),
                gcsFileMetadata.getEtag(),
                gcsFileMetadata.getLength(),
                gcsFileMetadata.getLastModified());
    }
}
