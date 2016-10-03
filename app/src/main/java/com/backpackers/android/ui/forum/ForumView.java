package com.backpackers.android.ui.forum;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseForumPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.framework.base.BaseMvpDataView;

public interface ForumView extends BaseMvpDataView<CollectionResponseForumPost> {

    void onPostSendingSuccessful(ForumPost post);

    void onPostSendingFailed(Throwable e);

    void onShowUploadNotification();

    void onDismissUploadNotification();

    void onVoteUpdate(ForumPost post);
}
