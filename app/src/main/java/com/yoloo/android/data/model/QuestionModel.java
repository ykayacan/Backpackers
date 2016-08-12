package com.yoloo.android.data.model;

import java.io.File;

public class QuestionModel {

    private long id;
    private long accountId;
    private String username;
    private String title;
    private String content;
    private String latLng;
    private String hashtag;
    private String location;
    private File file;
    private boolean isLiked;
    private boolean pending;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
