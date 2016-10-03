package com.backpackers.android.backend.util;

import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectMapper;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectWriter;

import com.backpackers.android.backend.response.WrappedCollectionResponse;
import com.backpackers.android.backend.response.WrappedErrorResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

public class ResponseUtil {

    private static final ObjectWriter ow =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static <T> void successResponse(final Collection<T> collection, final PrintWriter out)
            throws IOException {
        final String json = ow.writeValueAsString(
                WrappedCollectionResponse.<T>builder()
                        .setItems(collection)
                        .build());

        out.print(json);
    }

    public static void errorResponse(final String message, final PrintWriter out)
            throws IOException {
        final String json = ow.writeValueAsString(WrappedErrorResponse.builder()
                .setCode(HttpServletResponse.SC_BAD_REQUEST)
                .setMessage(message)
                .build());
        out.print(json);
    }
}
