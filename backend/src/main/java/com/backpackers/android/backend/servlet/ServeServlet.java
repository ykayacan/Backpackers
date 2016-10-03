package com.backpackers.android.backend.servlet;

import com.backpackers.android.backend.ShardedCounter;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServeServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("text/plain");

        //String counterName = req.getParameter("name");
        String action = req.getParameter("action");
        String shards = req.getParameter("shards");

        ShardedCounter counter = new ShardedCounter();

        if ("increment".equals(action)) {
            counter.increment();
            res.getWriter().println("Counter incremented.");
        } else if ("increase_shards".equals(action)) {
            int inc = Integer.parseInt(shards);
            counter.addShards(inc);
            res.getWriter().println("Shard count increased by " + inc + ".");
        } else {
            res.getWriter().println("getCount() -> " + counter.getCount());
        }
    }
}
