package com.backpackers.android.backend.servlet;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import com.backpackers.android.backend.model.notification.Notification;
import com.backpackers.android.backend.model.user.Account;
import com.googlecode.objectify.Key;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateNotificationFeedServlet extends HttpServlet {

    private static final String PARAM_MESSAGE_KEY = "message_key";
    private static final String PARAM_RECEIVER_KEY = "receiver_key";

    public static void create(Key<Notification> messageKey, Key<Account> receiverKey) {
        Queue queue = QueueFactory.getQueue("notification-queue");
        queue.add(TaskOptions.Builder
                .withUrl("/tasks/updaterank")
                .param(PARAM_MESSAGE_KEY, messageKey.toWebSafeString())
                .param(PARAM_RECEIVER_KEY, receiverKey.toWebSafeString()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        final String websafeMessageKey = req.getParameter(PARAM_MESSAGE_KEY);
        final String websafeReceiverKey = req.getParameter(PARAM_RECEIVER_KEY);


    }
}
