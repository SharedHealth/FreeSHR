package org.freeshr.interfaces.encounter.ws;


import com.sun.syndication.feed.atom.*;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.events.EncounterEvent;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.atomfeed.FeedBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EncounterFeedHelper {

    private static final String ATOM_MEDIA_TYPE = "application/atom+xml";
    private static final String LINK_TYPE_SELF = "self";
    private static final String LINK_TYPE_VIA = "via";
    private static final String ATOMFEED_MEDIA_TYPE = "application/vnd.atomfeed+xml";
    public static final String APPLICATION_XML = "application/xml";

    public Feed generateFeed(EncounterSearchResponse result, String feedId) {
        return new FeedBuilder()
                .type("atom_1.0")
                .id(feedId)
                .title("Patient Encounters")
                .generator(getGenerator())
                .authors(getAuthors())
                .entries(getEntries(result.getEntries()))
                .updated(newestEventDate(result.getEntries()))
                .link(getSelfLink(result.getRequestUrl()))
                .link(getViaLink(result.getRequestUrl()))
                .links(generatePagingLinks(result.getPrevUrl(), result.getNextUrl()))
                .build();
    }

    private Link getViaLink(String requestUrl) {
        //return getLink(generateCanonicalUri(request, feedId), LINK_TYPE_VIA, ATOM_MEDIA_TYPE);
        return getLink(requestUrl, LINK_TYPE_VIA, ATOM_MEDIA_TYPE);
    }

    private Link getSelfLink(String requestUrl) {
        return getLink(requestUrl, LINK_TYPE_SELF, ATOM_MEDIA_TYPE);
    }

    private Date newestEventDate(List<EncounterEvent> encounterEvents) {
        return (encounterEvents.size() > 0) ? DateUtil.parseDate(encounterEvents.get(0).getUpdatedDateISOString()) : null;
    }

    private List<Entry> getEntries(List<EncounterEvent> encounterEvents) {
        List<Entry> entryList = new ArrayList<Entry>();
        for (EncounterEvent encounterEvent : encounterEvents) {
            final Entry entry = new Entry();
            entry.setId(encounterEvent.getId());
            entry.setTitle("Encounter:" + encounterEvent.getEncounterId());

            Link encLink = new Link();
            encLink.setRel(LINK_TYPE_VIA);
            encLink.setType(APPLICATION_XML);
            encLink.setHref(encounterEvent.getLink());
            entry.setAlternateLinks(Arrays.asList(encLink));

            entry.setUpdated(encounterEvent.getCreatedAt());
            entry.setContents(generateContents(encounterEvent));
            entry.setCategories(getCategories(encounterEvent));
            entryList.add(entry);
        }
        return entryList;
    }

    private List<Category> getCategories(EncounterEvent encounterEvent) {
        List<Category> categories = new ArrayList<>();
        for (String categoryTerm : encounterEvent.getCategories()) {
            Category category = new Category();
            category.setTerm(categoryTerm);
            categories.add(category);
        }
        return categories;
    }

    private Generator getGenerator() {
        Generator generator = new Generator();
        generator.setUrl("https://github.com/ICT4H/atomfeed");
        generator.setValue("Atomfeed");
        return generator;
    }

    private List<Person> getAuthors() {
        Person person = new Person();
        person.setName("FreeSHR");
        return Arrays.asList(person);
    }

    private Link getLink(String href, String rel, String type) {
        Link link = new Link();

        link.setHref(href);
        link.setRel(rel);
        link.setType(type);

        return link;
    }

    private String generateCanonicalUri(URI requestUri, String feedId) {
        return getServiceUri(requestUri) + "/" + feedId;
    }

    private String getServiceUri(URI requestUri) {
        String scheme = requestUri.getScheme();
        String hostname = requestUri.getHost();
        int port = requestUri.getPort();
        String path = requestUri.getPath().substring(0,
                requestUri.getPath().lastIndexOf("/"));
        if (port != 80 && port != -1) {
            return scheme + "://" + hostname + ":" + port + path;
        } else {
            return scheme + "://" + hostname + path;
        }
    }

    private List<Link> generatePagingLinks(String prevLink, String nextLink) {
        ArrayList<Link> links = new ArrayList<Link>();
        if (!StringUtils.isBlank(nextLink)) {
            Link next = new Link();
            next.setRel("next-archive");
            next.setType(ATOM_MEDIA_TYPE);
            next.setHref(nextLink);
            links.add(next);
        }

        if (!StringUtils.isBlank(prevLink)) {
            Link prev = new Link();
            prev.setRel("prev-archive");
            prev.setType(ATOM_MEDIA_TYPE);
            prev.setHref(prevLink);
            links.add(prev);
        }
        return links;
    }

    private List<Content> generateContents(EncounterEvent encounterEvent) {
        Content content = new Content();
        content.setType(ATOMFEED_MEDIA_TYPE);
        content.setValue(wrapInCDATA(encounterEvent.getContent()));
        return Arrays.asList(content);
    }

    private String wrapInCDATA(String contents) {
        if (contents == null) {
            return null;
        }
        return String.format("%s%s%s", "<![CDATA[", contents, "]]>");
    }
}
