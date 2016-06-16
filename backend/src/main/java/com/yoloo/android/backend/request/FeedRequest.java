package com.yoloo.android.backend.request;

import com.google.api.server.spi.response.BadRequestException;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class FeedRequest {

    private String title;
    private String message;
    private String[] hashtags;
    private String location;
    private float latitude;
    private float longitude;
    private File[] files;

    public void validate() throws BadRequestException {
        if (this.title == null || this.title.isEmpty()) {
            throw new BadRequestException("Feed title cannot be empty.");
        }
        if (this.message == null || this.message.isEmpty()) {
            throw new BadRequestException("Feed message cannot be empty.");
        }
        if (this.location == null || this.location.isEmpty()) {
            throw new BadRequestException("Feed location cannot be empty.");
        }
        if (this.hashtags == null || this.hashtags.length == 0) {
            throw new BadRequestException("Feed hashtags cannot be empty.");
        }
        if (this.latitude == 0) {
            throw new BadRequestException("Feed latitude cannot be 0.");
        }
        if (this.longitude == 0) {
            throw new BadRequestException("Feed longitude cannot be 0.");
        }
    }
}
