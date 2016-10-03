package com.backpackers.android.ui.listeners;

import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;

import android.view.View;

public interface OnReadMoreClickListener {

    void onReadMoreClick(View v, int position, ForumPost post);
}
