package com.backpackers.android.ui.post_detail;

import com.backpackers.android.backend.modal.yolooApi.model.AbstractPost;
import com.backpackers.android.backend.modal.yolooApi.model.ForumPost;
import com.backpackers.android.framework.base.BaseMvpView;

public interface PostDetailView extends BaseMvpView {

    void onForumPostUpdate(ForumPost post);

    void onTimelinePostUpdate(AbstractPost post);

    void onVoteUpdate(ForumPost post);
}
