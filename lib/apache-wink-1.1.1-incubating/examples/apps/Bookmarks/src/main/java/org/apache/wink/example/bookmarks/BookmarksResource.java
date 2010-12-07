/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.example.bookmarks;

import java.net.URI;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.server.utils.LinkBuilders;

@Workspace(workspaceTitle = "Demo Bookmarks Service", collectionTitle = "My Bookmarks")
@Path("bookmarks")
public class BookmarksResource {

    private static final String BOOKMARK          = "bookmark";
    private static final String SUB_RESOURCE_PATH = "{" + BOOKMARK + "}";

    /**
     * This method is invoked when the HTTP GET method is issued by the client.
     * This occurs only when the requested representation (Http Accept header)
     * is Atom (application/atom+xml). The feed is created with the mandatory
     * fields. The feed will also contain the entries found in the
     * BookmarksStore. Links are generated for the feed and for the entries.
     * 
     * @param linksBuilders
     * @param uriInfo
     * @return syndFeed
     */
    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML})
    public SyndFeed getBookmarks(@Context LinkBuilders linksBuilders, @Context UriInfo uriInfo) {
        SyndFeed feed = new SyndFeed();
        feed.setId("urn:collection:bookmarks");
        feed.setTitle(new SyndText("My Bookmarks"));
        feed.setUpdated(new Date());
        feed.setBase(uriInfo.getAbsolutePath().toString());

        // add entries to the feed, based on the existing bookmarks in the
        // memory store
        // (feed entries have no content, they have just metadata so there is no
        // need to set content
        // here)
        Map<String, String> bookmarks = BookmarkStore.getInstance().getBookmarks();

        for (String key : bookmarks.keySet()) {

            SyndEntry entry = createEntry(key, bookmarks.get(key), uriInfo);

            // Generate system links to sub-resource
            linksBuilders.createSystemLinksBuilder().subResource(SUB_RESOURCE_PATH)
                .pathParam(BOOKMARK, key).build(entry.getLinks());

            // Add entry to Feed
            feed.addEntry(entry);
        }

        // Generate system links for the resource;
        linksBuilders.createSystemLinksBuilder().build(feed.getLinks());

        return feed;
    }

    /**
     * This method is invoked when the HTTP POST method is issued by the client.
     * This occurs only when the requested representation (Http Accept header)
     * is Atom (application/atom+xml) and plain text is provided in the HTTP
     * request message body of content MIME type "text/plain" (header
     * "Content-Type" must be "text/plain"). This method creates a new Bookmark
     * resource based on the data in the request and puts the new Bookmark into
     * the BookmarkStore.
     * 
     * @param bookmark to create
     * @param uriInfo object
     * @param linksBuilders object
     * @return Response
     */
    @POST
    @Consumes( {MediaType.TEXT_PLAIN})
    @Produces( {MediaType.APPLICATION_ATOM_XML})
    public Response createBookmark(String bookmark,
                                   @Context UriInfo uriInfo,
                                   @Context LinkBuilders linksBuilders) {
        if (bookmark == null || bookmark.length() == 0) {
            return Response.status(HttpStatus.BAD_REQUEST.getCode()).build();
        }

        String bookmarkId = BookmarkStore.getNewId();
        BookmarkStore.getInstance().putBookmark(bookmarkId, bookmark);

        SyndEntry entry = createEntry(bookmarkId, bookmark, uriInfo);

        // Generate system links to sub-resource
        linksBuilders.createSystemLinksBuilder().build(entry.getLinks());

        URI location = uriInfo.getAbsolutePathBuilder().segment(bookmarkId).build();

        return Response.created(location).entity(entry).build();
    }

    /**
     * This method is invoked when the HTTP GET method is issued by the client.
     * This occurs only when the requested representation (Http Accept header)
     * is Atom (application/atom+xml). In the case that the requested bookmark
     * is found in the BookmarkStore a synd entry is created with mandatory
     * metadata fields and with metadata content that is taken from the
     * BookmarkStore.
     * 
     * @param linksBuilders
     * @param uriInfo
     * @param bookmarkId the bookmark id to get as it appears on the request uri
     * @return SyndEntry with the information about the bookmark
     */
    @Path(SUB_RESOURCE_PATH)
    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML})
    public SyndEntry getBookmark(@Context LinkBuilders linksBuilders,
                                 @Context UriInfo uriInfo,
                                 @PathParam(BOOKMARK) String bookmarkId) {
        // check whether the bookmark exists in the memory store
        String bookmark = BookmarkStore.getInstance().getBookmark(bookmarkId);
        if (bookmark == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Create SyndEntry
        SyndEntry entry = createEntry(bookmarkId, bookmark, uriInfo);

        // Generate system links to sub-resource
        linksBuilders.createSystemLinksBuilder().relativize(true).build(entry.getLinks());

        return entry;
    }

    /**
     * This method is invoked when the HTTP PUT method is issued by the client.
     * This occurs only when the requested representation (Http Accept header)
     * is Atom (application/atom+xml) and plain text is provided in the HTTP
     * request message body of content MIME type "text/plain" (header
     * "Content-Type" must be "text/plain"). This method will update the
     * requested bookmark in the BookmarkStore with new content taken from the
     * request message body.
     * 
     * @param bookmarkId the bookmark id to update as it appears on the request
     *            uri
     * @return SyndEntry with the information about the updated bookmark
     * @throws Exception a problem with reading the input stream
     */
    @Path(SUB_RESOURCE_PATH)
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces( {MediaType.APPLICATION_ATOM_XML})
    public SyndEntry updateBookmark(String bookmark,
                                    @Context LinkBuilders linksBuilders,
                                    @Context UriInfo uriInfo,
                                    @PathParam(BOOKMARK) String bookmarkId) {
        // check whether the bookmark exists for update
        String value = BookmarkStore.getInstance().getBookmark(bookmarkId);
        if (value == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // update existing bookmark in the memory store with new bookmark value
        BookmarkStore.getInstance().putBookmark(bookmarkId, bookmark);

        // Create SyndEntry
        SyndEntry entry = createEntry(bookmarkId, bookmark, uriInfo);

        // Generate system links to sub-resource
        linksBuilders.createSystemLinksBuilder().build(entry.getLinks());

        return entry;
    }

    /**
     * This method is invoked when the HTTP DELETE method is issued by the
     * client. This occurs only when the requested representation (Http Accept
     * header) is Atom (application/atom+xml). This method deletes the bookmark
     * from the BookmarkStore and returns the deleted bookmark.
     * 
     * @param linksBuilders
     * @param uriInfo
     * @param bookmarkId the bookmark id to update as it appears on the request
     *            uri
     * @return SyndEntry with the information about the deleted bookmark
     */
    @Path(SUB_RESOURCE_PATH)
    @DELETE
    @Produces( {MediaType.APPLICATION_ATOM_XML})
    public SyndEntry deleteBookmark(@Context LinkBuilders linksBuilders,
                                    @Context UriInfo uriInfo,
                                    @PathParam(BOOKMARK) String bookmarkId) {

        // check whether the bookmark exists for deletion
        String bookmark = BookmarkStore.getInstance().getBookmark(bookmarkId);
        if (bookmark == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Remove bookmark form the store
        BookmarkStore.getInstance().deleteBookmark(bookmarkId);

        // create SyndEntry and return it
        SyndEntry entry = createEntry(bookmarkId, bookmark, uriInfo);

        // Generate system links to sub-resource
        linksBuilders.createSystemLinksBuilder().build(entry.getLinks());

        return entry;
    }

    private SyndEntry createEntry(String bookmarkId, String content, UriInfo uriInfo) {
        SyndEntry entry = new SyndEntry();
        entry.setId(bookmarkId);
        entry.setTitle(new SyndText("My Bookmark " + bookmarkId));
        entry.setPublished(new Date());
        entry.setBase(uriInfo.getAbsolutePath().toString());

        if (content != null) {
            entry.setContent(new SyndContent(content));
        }
        return entry;
    }

}
