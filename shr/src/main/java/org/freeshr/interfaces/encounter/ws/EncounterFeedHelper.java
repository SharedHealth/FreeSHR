package org.freeshr.interfaces.encounter.ws;


import com.sun.syndication.feed.atom.*;
import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.atomfeed.FeedBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.text.SimpleDateFormat;
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

    private Date newestEventDate(List<EncounterBundle> encounters) {
        return (encounters.size() > 0) ? DateUtil.parseDate(encounters.get(0).getReceivedDate()) : null;
    }

    private List<Entry> getEntries(List<EncounterBundle> encounters) {
        List<Entry> entryList = new ArrayList<Entry>();
        for (EncounterBundle encounter : encounters) {
            final Entry entry = new Entry();
            entry.setId(encounter.getEncounterId());
            entry.setTitle("Encounter:" + encounter.getEncounterId());

            Link encLink = new Link();
            encLink.setRel(LINK_TYPE_VIA);
            encLink.setType(APPLICATION_XML);
            encLink.setHref(encounter.getLink());
            entry.setAlternateLinks(Arrays.asList(encLink));


            entry.setUpdated(DateUtil.parseDate(encounter.getReceivedDate()));
            entry.setContents(generateContents(encounter));
            Category category = new Category();
            category.setTerm("encounter");
            entry.setCategories(Arrays.asList(category));
            entryList.add(entry);
        }
        return entryList;
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

    private List<Content> generateContents(EncounterBundle encounter) {
        Content content = new Content();
        content.setType(ATOMFEED_MEDIA_TYPE);
        content.setValue(wrapInCDATA(encounter.getContent()));
        return Arrays.asList(content);
    }

    private String wrapInCDATA(String contents){
        if(contents == null){
            return null;
        }
        return String.format("%s%s%s","<![CDATA[",contents,"]]>");
    }
}
