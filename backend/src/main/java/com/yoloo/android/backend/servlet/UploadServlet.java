package com.yoloo.android.backend.servlet;

import com.google.api.client.util.Strings;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectMapper;
import com.google.appengine.repackaged.org.codehaus.jackson.map.ObjectWriter;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.io.ByteStreams;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.yoloo.android.backend.model.media.Media;
import com.yoloo.android.backend.model.media.NormalVideoRes;
import com.yoloo.android.backend.model.media.Resolution;
import com.yoloo.android.backend.model.media.photo.LowPhotoRes;
import com.yoloo.android.backend.model.media.photo.NormalPhotoRes;
import com.yoloo.android.backend.model.media.photo.OriginalPhotoRes;
import com.yoloo.android.backend.model.media.photo.ThumbPhotoRes;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.response.WrappedCollectionResponse;
import com.yoloo.android.backend.response.WrappedErrorResponse;
import com.yoloo.android.backend.util.MimeUtil;
import com.yoloo.android.backend.util.RandomGenerator;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class UploadServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(UploadServlet.class.getName());

    private static final String ACL = "public-read";

    private static final String SEP = "/";

    private static final String BASE_BUCKETNAME = "yoloo-app.appspot.com";

    private static final String MEDIA_BUCKET =
            BASE_BUCKETNAME + SEP + "media";

    private static final String MEDIA_BUCKET_PHOTO =
            MEDIA_BUCKET + SEP + "photo";

    private static final String MEDIA_BUCKET_VIDEO =
            MEDIA_BUCKET + SEP + "video";

    private static final String WEBSAFE_USER_ID = "websafeUserId";

    private static final ServletFileUpload FILE_UPLOAD = new ServletFileUpload();

    private static final ObjectWriter ow =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    private static final GcsService gcsService = GcsServiceFactory
            .createGcsService(new RetryParams.Builder()
                    .initialRetryDelayMillis(10)
                    .retryMaxAttempts(10)
                    .totalRetryPeriodMillis(15000)
                    .build());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        final String websafeUserId = req.getParameter(WEBSAFE_USER_ID);

        setResponse(resp);

        parseRequest(req, resp, websafeUserId);
    }

    private void parseRequest(HttpServletRequest req,
                              HttpServletResponse resp,
                              String websafeUserId)
            throws IOException {

        final List<Media> medias = new ArrayList<>(3);

        PrintWriter out = resp.getWriter();

        // Check if the request is actually a multipart/form-data request.
        if (!ServletFileUpload.isMultipartContent(req)) {
            errorResponse("This isn't a multipart request.", out);
            // If not, then return the null.
            return;
        }

        if (!isValidUserId(req.getParameter(WEBSAFE_USER_ID))) {
            errorResponse("Invalid user id.", out);
            return;
        }

        final Key<Account> userKey = Key.create(websafeUserId);

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

    private void processUploadedFile(Key<Account> userKey, List<Media> medias,
                                     InputStream is, String mimeType)
            throws IOException {
        final GcsFileOptions options = getGcsFileOptions(mimeType);

        if (MimeUtil.isPhoto(mimeType)) {
            processPhotoMedia(userKey, medias, is, options, mimeType);
        } else {
            processVideoMedia(userKey, medias, is, options, mimeType);
        }
    }

    private void processVideoMedia(Key<Account> userKey, List<Media> medias, InputStream is,
                                   GcsFileOptions options, String mimeType)
            throws IOException {
        GcsFilename gcsFilename = getGcsFilename("video", mimeType, userKey.toWebSafeString());

        gcsService.createOrReplace(gcsFilename, options,
                ByteBuffer.wrap(ByteStreams.toByteArray(is)));

        GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);

        final Media media = new Media.Builder(userKey)
                .setMeta(metadata)
                .setData(new Resolution(NormalVideoRes.from(gcsFilename)))
                .build();

        System.out.println("Bucket: " + gcsFilename.getBucketName() + "\n"
                + " filename: " + gcsFilename.getObjectName());

        medias.add(media);
    }

    private void processPhotoMedia(Key<Account> userKey, List<Media> medias, InputStream is,
                                   GcsFileOptions options, String mimeType)
            throws IOException {
        GcsFilename gcsFilename = getGcsFilename("photo", mimeType, userKey.toWebSafeString());

        gcsService.createOrReplace(gcsFilename, options,
                ByteBuffer.wrap(ByteStreams.toByteArray(is)));

        GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);

        final Resolution data = new Resolution(
                LowPhotoRes.from(gcsFilename),
                NormalPhotoRes.from(gcsFilename),
                OriginalPhotoRes.from(gcsFilename),
                ThumbPhotoRes.from(gcsFilename));

        final Media media = new Media.Builder(userKey)
                .setMeta(metadata)
                .setData(data)
                .build();

        medias.add(media);
    }

    private void setResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "nocache");
        resp.setCharacterEncoding("UTF-8");
    }

    private GcsFileOptions getGcsFileOptions(String mimeType) {
        return new GcsFileOptions.Builder()
                .mimeType(mimeType)
                .acl(ACL)
                .build();
    }

    private String createFileName(String mimeType) {
        return RandomGenerator.INSTANCE.generate() + "." + extractExtension(mimeType);
    }

    private String extractExtension(String mimeType) {
        return mimeType.substring(mimeType.indexOf("/") + 1);
    }

    private GcsFilename getGcsFilename(String type, String mimeType, String websafeUserId) {
        return new GcsFilename(
                MEDIA_BUCKET + SEP + type + SEP + websafeUserId,
                createFileName(mimeType));
    }

    private void successResponse(Collection<Media> medias, PrintWriter out) throws IOException {
        String json = ow.writeValueAsString(
                WrappedCollectionResponse.<Media>builder()
                        .setItems(medias)
                        .build());

        out.print(json);
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

    private String fileName(GcsFilename gcsFilename) {
        return "/gs/" + gcsFilename.getBucketName() + "/" + gcsFilename.getObjectName();
    }
}
