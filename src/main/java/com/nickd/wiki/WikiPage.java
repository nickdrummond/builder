package com.nickd.wiki;

import com.nickd.util.App;
import com.nickd.wiki.creator.Creator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WikiPage {

    private final Logger logger = LoggerFactory.getLogger(WikiPage.class);

//    private final List<String> linkSelectors = List.of(
//            "aside.portable-infobox a",
//            "#mw-content-text p a"
//    );

    private final App app;
    private OWLAnnotationProperty seeAlso;
    private final Document doc;

    private final List<Creator<? extends OWLEntity>> selectors;

    // Retain order of the links for more pertinent suggestions
    private final LinkedHashMap<OWLEntity, String> knownEntities = new LinkedHashMap<>();
    private final LinkedHashMap<OWLEntity, String> suggestions = new LinkedHashMap<>();

    WikiPage(App app, InputStream input, List<Creator<? extends OWLEntity>> selectors) throws IOException {
        this.app = app;
        this.doc = Jsoup.parse(input, StandardCharsets.UTF_8.name(), "");
        this.selectors = selectors;

//        indexEntity(iri.toString(), creator); // TODO index self

        buildLinksIndex(); // index links
    }

    private void buildLinksIndex() {

//        suggestType(doc).forEach(t -> System.out.println(app.render(t)));

        selectors.forEach(creator -> {
            creator.build(this);
        });
    }

    // Will only work with species
//    private Set<OWLClass> suggestType(Document doc) {
//        Set<String> likelyTypes = Set.of("Species");
//
//        Elements data = doc.select("aside.portable-infobox .pi-data");
//
//        for (Element d : data) {
//            String property = d.select(".pi-data-label").first().text();
//            Element value = d.select(".pi-data-value").first();
////            String valueString = value.text();
//            String valueLink = value.select("a[href]").first().text();
//            System.out.println(property + " = " + valueLink);
//            if (likelyTypes.contains(property)) {
//                Set<OWLClass> clses = FinderUtils.annotationExact(valueLink, seeAlso, app).stream()
//                        .filter(OWLEntity::isOWLClass)
//                        .map(AsOWLClass::asOWLClass)
//                        .collect(Collectors.toSet());
//                return clses;
//
//            }
//        }
//        return Collections.emptySet();
//    }


    public List<OWLEntity> getKnown() {
        return new ArrayList<>(knownEntities.keySet());
    }

    public List<OWLEntity> getUnknown() {
        return new ArrayList<>(suggestions.keySet());
    }

    public Document getDocument() {
        return doc;
    }

    public App getHelper() {
        return app;
    }

    public void addSuggestion(OWLEntity entity, String iriString) {
        suggestions.put(entity, iriString);
    }

    public void addKnownEntities(OWLEntity entity, String iriString) {
        knownEntities.put(entity, iriString);
    }
}
