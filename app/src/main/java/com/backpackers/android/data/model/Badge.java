package com.backpackers.android.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Badge implements Parcelable {

    public static final Creator<Badge> CREATOR = new Creator<Badge>() {
        @Override
        public Badge createFromParcel(Parcel in) {
            return new Badge(in);
        }

        @Override
        public Badge[] newArray(int size) {
            return new Badge[size];
        }
    };

    private String mName;
    private String mImageUrl;

    public Badge(String name, String imageUrl) {
        mName = name;
        mImageUrl = imageUrl;
    }

    protected Badge(Parcel in) {
        mName = in.readString();
        mImageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
