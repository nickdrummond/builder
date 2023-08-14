package com.nickd.wiki;

import com.nickd.builder.Constants;
import com.nickd.wiki.creator.Creator;
import com.nickd.util.CurlUtils;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WikiPage {

    private final Logger logger = LoggerFactory.getLogger(WikiPage.class);

//    private final List<String> linkSelectors = List.of(
//            "aside.portable-infobox a",
//            "#mw-content-text p a"
//    );

    private final Helper helper;
    private final List<Creator> selectors;
    private OWLAnnotationProperty seeAlso;
    private Document doc;

    // Retain order of the links for more pertinent suggestions
    private final LinkedHashMap<OWLEntity, String> knownEntities = new LinkedHashMap<>();
    private final LinkedHashMap<OWLEntity, String> suggestions = new LinkedHashMap<>();
    private IRI iri;

    WikiPage(Helper helper, IRI iri, List<Creator> selectors) throws IOException {
        this.helper = helper;
        this.iri = iri;
        this.doc = getFromWebOrCache(iri);
        this.selectors = selectors;

//        indexEntity(iri.toString(), creator); // TODO index self

        buildLinksIndex(); // index links
    }

    private void buildLinksIndex() {

        suggestType(doc).forEach(t -> System.out.println(helper.render(t)));

        selectors.forEach(creator -> {
            creator.build(this);
        });
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

    public String wikiPageName(IRI iri) {
        String path = iri.toURI().getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public List<OWLEntity> getKnown() {
        return new ArrayList<>(knownEntities.keySet());
    }

    public List<OWLEntity> getUnknown() {
        return new ArrayList<>(suggestions.keySet());
    }

    public IRI getIri() {
        return iri;
    }

    public Document getDocument() {
        return doc;
    }

    public Helper getHelper() {
        return helper;
    }

    public void addSuggestion(OWLEntity entity, String iriString) {
        suggestions.put(entity, iriString);
    }

    public void addKnownEntities(OWLEntity entity, String iriString) {
        knownEntities.put(entity, iriString);
    }
}
