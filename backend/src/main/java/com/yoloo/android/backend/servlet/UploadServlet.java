package com.yoloo.android.backend.servlet;

import com.google.api.client.util.Strings;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import com.googlecode.objectify.Key;
import com.yoloo.android.backend.model.media.Media;
import com.yoloo.android.backend.model.media.MediaToken;
import com.yoloo.android.backend.model.user.Account;
import com.yoloo.android.backend.util.RandomGenerator;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yoloo.android.backend.service.OfyHelper.ofy;

public class UploadServlet extends HttpServlet {

    private static final Logger logger =
            Logger.getLogger(UploadServlet.class.getName());

    /** Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private static final ServletFileUpload FILE_UPLOAD = new ServletFileUpload();

    private static final String ACL = "public-read";

    private static final String BASE_BUCKETNAME = "yoloo-app.appspot.com";

    private static final String MEDIA_BUCKET = BASE_BUCKETNAME + "/" + "media";

    private static final String WEBSAFE_USER_ID = "websafeUserId";

    private final GcsService gcsService = GcsServiceFactory
            .createGcsService(new RetryParams.Builder()
                    .initialRetryDelayMillis(10)
                    .retryMaxAttempts(10)
                    .totalRetryPeriodMillis(15000)
                    .build());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String token = req.getHeader("X-Upload-Token");
        final String websafeUserId = req.getParameter(WEBSAFE_USER_ID);

        final Key<Account> userKey = Key.create(websafeUserId);

        final PrintWriter out = resp.getWriter();

        if (Strings.isNullOrEmpty(token)) {
            out.print("X-Upload-Token is empty.");
            out.close();
            return;
        }

        if (!ServletFileUpload.isMultipartContent(req)) {
            out.print("This isn't a multipart request.");
            out.close();
            return;
        }

        final Key<MediaToken> tokenKey = Key.create(token);

        // Parse the request
        final FileItemIterator iterator;
        final List<Media> medias = new ArrayList<>();

        try {
            iterator = FILE_UPLOAD.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream fileItem = iterator.next();
                InputStream stream = fileItem.openStream();

                if (!fileItem.isFormField()) {
                    GcsFileOptions gcsFileOptions = new GcsFileOptions.Builder()
                            .mimeType(fileItem.getContentType())
                            .acl(ACL)
                            .build();

                    GcsFilename gcsFilename = new GcsFilename(MEDIA_BUCKET,
                            RandomGenerator.INSTANCE.generate());

                    GcsOutputChannel outputChannel =
                            gcsService.createOrReplace(gcsFilename, gcsFileOptions);

                    copy(stream, Channels.newOutputStream(outputChannel));

                    GcsFileMetadata metadata = gcsService.getMetadata(gcsFilename);

                    final Media media = new Media.Builder(userKey)
                            .setContentType(metadata.getOptions().getMimeType())
                            .setSize(metadata.getLength())
                            .build();

                    medias.add(media);
                }
            }

            ofy().delete().key(tokenKey).now();

            ofy().save().entities(medias).now();

            for (Media m : medias) {
                out.print(m.getWebsafeId());
            }
            out.close();
        } catch (FileUploadException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    private void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
