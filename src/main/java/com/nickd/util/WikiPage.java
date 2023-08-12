package com.nickd.util;

import com.nickd.builder.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class WikiPage {

    private final Logger logger = LoggerFactory.getLogger(WikiPage.class);

    private final List<String> linkSelectors = List.of(
            "aside.portable-infobox a",
            "#mw-content-text p a");

    private final Helper helper;
    private OWLAnnotationProperty seeAlso;
    private Document doc;

    // Retain order of the links for more pertinent suggestions
    private final LinkedHashMap<OWLEntity, String> knownEntities = new LinkedHashMap<>();
    private final LinkedHashMap<OWLEntity, String> suggestions = new LinkedHashMap<>();
    private IRI iri;

    WikiPage(Helper helper, IRI iri) throws IOException {
        this.helper = helper;
        this.iri = iri;
        this.doc = getFromWebOrCache(iri);

        indexEntity(iri.toString()); // index self
        buildLinksIndex(iri); // index links
    }

    private void buildLinksIndex(IRI iri) {

        URI uri = iri.toURI();
        String path = uri.getPath();
        String base = path.substring(0, path.lastIndexOf("/"));
        String root = uri.getScheme() + "://" + uri.getAuthority();

        suggestType(doc).forEach(t -> System.out.println(helper.render(t)));

        linkSelectors.stream()
                .map(s -> doc.select(s))
                .flatMap(Collection::stream)
                .map(l -> l.attr("href"))
                .distinct()
                .filter(h -> h.startsWith(base))
                .map(h -> (h.startsWith("/") ? root + h : h))
                .forEach(this::indexEntity);
    }

    private void indexEntity(String href) {
        List<OWLEntity> matches = FinderUtils.annotationExact(href, seeAlso, helper);
        if (matches.isEmpty()) {
            OWLNamedIndividual ind = addSuggestion(href);
            suggestions.put(ind, href);
        } else {
            matches.forEach(e -> knownEntities.put(e, href));
        }
    }

    private OWLNamedIndividual addSuggestion(String href) {
        OWLAnnotationProperty editorLabel = helper.annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        EntityBuilder entityBuilder = new EntityBuilder(helper, editorLabel);
        return entityBuilder.build(null, wikiPageName(IRI.create(href)), href, helper.suggestions);
    }

    // Will only work with species
    private Set<OWLClass> suggestType(Document doc) {
        Set<String> likelyTypes = Set.of("Species");

        Elements data = doc.select("aside.portable-infobox .pi-data");

        for (Element d : data) {
            String property = d.select(".pi-data-label").first().text();
            Element value = d.select(".pi-data-value").first();
//            String valueString = value.text();
            String valueLink = value.select("a[href]").first().text();
            System.out.println(property + " = " + valueLink);
            if (likelyTypes.contains(property)) {
                Set<OWLClass> clses = FinderUtils.annotationExact(valueLink, seeAlso, helper).stream()
                        .filter(OWLEntity::isOWLClass)
                        .map(AsOWLClass::asOWLClass)
                        .collect(Collectors.toSet());
                return clses;

            }
        }
        return Collections.emptySet();
    }

    private Document getFromWebOrCache(IRI iri) throws IOException {
        File cacheFile = cacheFileFor(iri);

        if (!cacheFile.exists()) {
            CurlUtils.curl(iri, cacheFile);
        }
        return Jsoup.parse(cacheFile);
    }

    private File cacheFileFor(IRI iri) {
        return new File(Constants.CACHES + wikiPageName(iri) + ".html");
    }

    private String wikiPageName(IRI iri) {
        String path = iri.toURI().getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public List<OWLEntity> getKnown() {
        return new ArrayList<>(knownEntities.keySet());
    }

    public List<OWLEntity> getUnknown() {
        return new ArrayList<>(suggestions.keySet());
    }

    public String getUrl() {
        return null;
    }

    public IRI getIri() {
        return iri;
    }
}
