package org.freeshr.utils.atomfeed;


import com.sun.syndication.feed.atom.*;
import org.freeshr.application.fhir.EncounterBundle;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AtomFeedHelper {

    private static final String ATOM_MEDIA_TYPE = "application/atom+xml";
    private static final String LINK_TYPE_SELF = "self";
    private static final String LINK_TYPE_VIA = "via";
    private static final String ATOMFEED_MEDIA_TYPE = "application/vnd.atomfeed+xml";

    public class NavigationLink {
        String prev;
        String next;
        public NavigationLink(String prev, String next) {
            this.prev = prev;
            this.next = next;
        }
        public String getPrev() {
            return prev;
        }
        public String getNext() {
            return next;
        }
    }

    public Feed generateFeed(URI requestUri, List<EncounterBundle> encounters, Date dateOfFeed, String catchment, NavigationLink navigationLink) {
        String feedId = generateIdForEventFeed(dateOfFeed, catchment);
        return new FeedBuilder()
                .type("atom_1.0")
                .id(feedId)
                .title("Patient Encounters")
                .generator(getGenerator())
                .authors(getAuthors())
                .entries(getEntries(encounters))
                .updated(newestEventDate(encounters))
                .link(getLink(requestUri.toString(), LINK_TYPE_SELF, ATOM_MEDIA_TYPE))
                .link(getLink(generateCanonicalUri(requestUri, feedId), LINK_TYPE_VIA, ATOM_MEDIA_TYPE))
                .links(generatePagingLinks(navigationLink))
                .build();
    }

    private Date newestEventDate(List<EncounterBundle> encounters) {
        return null;
    }

    private List<Entry> getEntries(List<EncounterBundle> encounters) {
        return null;
    }

    private String generateIdForEventFeed(Date date, String catchment){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date) + "+" + catchment;
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

    private List<Link> generatePagingLinks(NavigationLink navigationLink) {
        ArrayList<Link> links = new ArrayList<Link>();
        if (navigationLink == null) return links;

        if (navigationLink.getNext() != null) {
            Link next = new Link();
            next.setRel("next-archive");
            next.setType(ATOM_MEDIA_TYPE);
            next.setHref(navigationLink.getNext());
            links.add(next);
        }

        if (navigationLink.getPrev() != null) {
            Link prev = new Link();
            prev.setRel("prev-archive");
            prev.setType(ATOM_MEDIA_TYPE);
            prev.setHref(navigationLink.getPrev());
            links.add(prev);
        }
        return links;
    }
}
