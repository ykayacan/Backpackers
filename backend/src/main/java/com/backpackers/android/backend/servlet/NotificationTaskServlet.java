package com.backpackers.android.backend.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotificationTaskServlet extends HttpServlet {

    private static final String WEBSAFE_RECEIVER_ID = "websafeReceiverId";
    private static final String ACTION_TYPE = "ACTION_TYPE";
    private static final String WEBSAFE_POST_ID = "websafePostId";
    private static final String CONTENT = "CONTENT";
    private static final String WEBSAFE_COMMENT_ID = "WEBSAFE_COMMENT_ID";

    public static void create(String websafeReceiverId, String actionType,
                              String websafePostId, String content,
                              String websafeCommentId) {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder
                .withUrl("/tasks/sendfcm")
                .param(WEBSAFE_RECEIVER_ID, websafeReceiverId)
                .param(WEBSAFE_POST_ID, websafePostId)
                .param(ACTION_TYPE, actionType)
                .param(CONTENT, content)
                .param(WEBSAFE_COMMENT_ID, websafeCommentId));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
        final String websafeReceiverId = req.getParameter(WEBSAFE_RECEIVER_ID);
        final String websafePostId = req.getParameter(WEBSAFE_POST_ID);
        final String actionType = req.getParameter(ACTION_TYPE);
        final String content = req.getParameter(CONTENT);
        final String websafeCommentId = req.getParameter(WEBSAFE_COMMENT_ID);

        switch (actionType) {
            case "MENTION":

                break;
        }
    }
}
