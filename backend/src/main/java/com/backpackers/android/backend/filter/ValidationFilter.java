package com.backpackers.android.backend.filter;

import com.google.api.client.util.Strings;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectMapper;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectWriter;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.response.WrappedErrorResponse;
import com.backpackers.android.backend.util.MimeUtil;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

public class ValidationFilter implements Filter {

    private static final String WEBSAFE_USER_ID = "websafeUserId";

    private static final ObjectWriter ow =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final ServletFileUpload FILE_UPLOAD = new ServletFileUpload();

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        // Check request type.
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;

            setResponse(resp);

            // Parse HttpServletRequest.
            boolean isValid = parseRequest(req, resp);

            if (isValid) {
                // Continue with filter chain.
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {

    }

    private boolean parseRequest(HttpServletRequest req,
                                            HttpServletResponse resp)
            throws IOException {
        final PrintWriter out = resp.getWriter();

        // Check if the request is actually a multipart/form-data request.
        if (!ServletFileUpload.isMultipartContent(req)) {
            errorResponse("This isn't a multipart request.", out);
            // If not, then return the null.
            return false;
        }

        if (!isValidUserId(req.getParameter(WEBSAFE_USER_ID))) {
            errorResponse("Invalid user id.", out);
            return false;
        }

        try {
            final FileItemIterator it = FILE_UPLOAD.getItemIterator(req);

            while (it.hasNext()) {
                final FileItemStream multipartItem = it.next();

                if (!multipartItem.isFormField()) {
                    if (!MimeUtil.isValidMime(multipartItem.getContentType())) {
                        errorResponse("It is not a valid type.", out);
                        return false;
                    }

                    if (multipartItem.getName().length() <= 0) {
                        errorResponse("File is not uploaded.", out);
                        return false;
                    }
                }
            }
        } catch (FileUploadException e) {
            errorResponse(e.getMessage(), out);
            return false;
        }

        return true;
    }

    private void setResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "nocache");
        resp.setCharacterEncoding("UTF-8");
    }

    private boolean isValidUserId(String websafeUserId) {
        if (Strings.isNullOrEmpty(websafeUserId)) {
            return false;
        }

        try {
            checkIsNotFound(Key.<Account>create(websafeUserId));
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }

    private void checkIsNotFound(Key<?> key)
            throws NotFoundException {
        ofy().load().kind(key.getKind()).filter("__key__ =", key)
                .keys().first().safe();
    }

    private void errorResponse(String message, PrintWriter out)
            throws IOException {
        String json = ow.writeValueAsString(WrappedErrorResponse.builder()
                .setCode(HttpServletResponse.SC_BAD_REQUEST)
                .setMessage(message)
                .build());
        out.print(json);
    }
}
