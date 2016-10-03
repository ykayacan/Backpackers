package com.backpackers.android.backend.servlet;

import com.google.api.client.util.Strings;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.io.ByteStreams;

import com.backpackers.android.backend.GcsSingleton;
import com.backpackers.android.backend.mapper.Mapper;
import com.backpackers.android.backend.util.MediaUtil;
import com.googlecode.objectify.Key;
import com.backpackers.android.backend.mapper.MetadataMapper;
import com.backpackers.android.backend.model.media.Media;
import com.backpackers.android.backend.model.media.Metadata;
import com.backpackers.android.backend.model.user.Account;
import com.backpackers.android.backend.util.MimeUtil;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.backpackers.android.backend.service.OfyHelper.ofy;
import static com.backpackers.android.backend.util.ResponseUtil.errorResponse;
import static com.backpackers.android.backend.util.ResponseUtil.successResponse;

public class UploadServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(UploadServlet.class.getName());

    private static final String ACL = "public-read";
    private static final String CACHE_CONTROL = "public,max-age=3600";

    private static final String USER_ID = "token";

    private static final ServletFileUpload FILE_UPLOAD = new ServletFileUpload();

    private final Mapper<GcsFileMetadata, Metadata> mapper = new MetadataMapper();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String accessToken = req.getParameter(USER_ID);

        setResponse(resp);

        parseRequest(req, resp, accessToken);
    }

    private void parseRequest(HttpServletRequest req, HttpServletResponse resp, String userId)
            throws IOException {

        PrintWriter out = resp.getWriter();

        // Check if the request is actually a multipart/form-data request.
        if (!ServletFileUpload.isMultipartContent(req)) {
            errorResponse("This isn't a multipart request.", out);
            return;
        }

        if (Strings.isNullOrEmpty(req.getParameter(USER_ID))) {
            errorResponse("User id is empty", out);
            return;
        }

        final Key<Account> userKey = ofy().load().type(Account.class)
                .filter("__key__ =", Key.create(userId)).keys().first().now();
        if (userKey == null) {
            errorResponse("User id is invalid.", out);
            return;
        }

        final List<Media> medias = new ArrayList<>(3);

        try {
            final FileItemIterator it = FILE_UPLOAD.getItemIterator(req);

            while (it.hasNext()) {
                final FileItemStream multipartItem = it.next();

                if (!multipartItem.isFormField()) {
                    if (!MimeUtil.isValidMime(multipartItem.getContentType())) {
                        errorResponse("It is not a valid type.", out);
                        return;
                    }

                    if (multipartItem.getName().length() <= 0) {
                        errorResponse("File is not uploaded.", out);
                        return;
                    }

                    final InputStream is = multipartItem.openStream();
                    final String mimeType = multipartItem.getContentType();

                    processUploadedFile(userKey, medias, is, mimeType);
                }
            }

            ofy().save().entities(medias).now();

            successResponse(medias, out);
        } catch (FileUploadException e) {
            errorResponse(e.getMessage(), out);
        }
    }

    private void processUploadedFile(final Key<Account> userKey, final List<Media> medias,
                                     final InputStream is, final String mimeType)
            throws IOException {
        final GcsFileOptions options = new GcsFileOptions.Builder()
                .mimeType(mimeType)
                .acl(ACL)
                .cacheControl(CACHE_CONTROL)
                .build();

        final GcsFilename gcsFilename =
                MediaUtil.createGcsFilename(mimeType, userKey.toWebSafeString());

        final GcsService gcsService = GcsSingleton.getGcsService();

        gcsService.createOrReplace(gcsFilename, options,
                ByteBuffer.wrap(ByteStreams.toByteArray(is)));

        final GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);

        final Media media = new Media.Builder(userKey)
                .setMeta(mapper.map(metadata))
                .build();

        medias.add(media);
    }

    private void setResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "nocache");
        resp.setCharacterEncoding("UTF-8");
    }
}
