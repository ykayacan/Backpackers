package com.backpackers.android.backend.servlet;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.StatusCode;

import com.backpackers.android.backend.model.location.Location;
import com.backpackers.android.backend.util.LocationHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.backpackers.android.backend.service.OfyHelper.ofy;

/**
 * HttpServlet for handling maintenance tasks.
 */
public class MaintenanceTasksServlet extends HttpServlet {

    @Override
    public final void doGet(final HttpServletRequest req,
                            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        if (!buildSearchIndexForPlaces()) {
            resp.getWriter().println(
                    "MaintenanceTasks failed. Try again by refreshing.");
            return;
        }
        resp.getWriter().println("MaintenanceTasks completed");
    }

    /**
     * Creates the indexes to search for places.
     * @return a boolean indicating the success or failure of the method.
     */
    @SuppressWarnings({"cast", "unchecked"})
    private boolean buildSearchIndexForPlaces() {
        Index index = LocationHelper.getIndex();

        removeAllDocumentsFromIndex();

        List<Location> places = ofy().load().type(Location.class).list();

        try {
            for (Location place : places) {
                Document placeAsDocument = LocationHelper.buildDocument(
                        place.getId(), place.getName(), place.getAddress(),
                        place.getLocation());
                try {
                    index.put(placeAsDocument);
                } catch (PutException e) {
                    if (StatusCode.TRANSIENT_ERROR
                            .equals(e.getOperationResult().getCode())) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Cleans the index of places from all entries.
     */
    private void removeAllDocumentsFromIndex() {
        Index index = LocationHelper.getIndex();
        // As the request will only return up to 1000 documents,
        // we need to loop until there are no more documents in the index.
        // We batch delete 1000 documents per iteration.
        final int numberOfDocuments = 1000;
        while (true) {
            GetRequest request = GetRequest.newBuilder()
                    .setReturningIdsOnly(true)
                    .build();

            ArrayList<String> documentIds = new ArrayList<>(numberOfDocuments);
            GetResponse<Document> response = index.getRange(request);
            for (Document document : response.getResults()) {
                documentIds.add(document.getId());
            }

            if (documentIds.size() == 0) {
                break;
            }

            index.delete(documentIds);
        }
    }
}
