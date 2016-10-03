package com.backpackers.android.ui.comment;

import com.backpackers.android.backend.modal.yolooApi.model.CollectionResponseComment;
import com.backpackers.android.backend.modal.yolooApi.model.Comment;
import com.backpackers.android.framework.base.BaseMvpDataView;
import com.backpackers.android.ui.user_autocomplete.AutocompleteMentionItem;

import java.util.List;

public interface CommentView extends BaseMvpDataView<CollectionResponseComment> {

    void onCommentSuccessful(Comment commentId);

    void onMentionListArrived(List<AutocompleteMentionItem> items);
}
