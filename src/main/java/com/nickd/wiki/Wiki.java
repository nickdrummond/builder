package com.nickd.wiki;

import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import com.nickd.wiki.creator.ClassCreator;
import com.nickd.wiki.creator.Creator;
import com.nickd.wiki.creator.IndividualCreator;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Wiki {

    // Is this needed - see https://jsoup.org/cookbook/extracting-data/working-with-urls
    public static final String WIKI = "https://starwars.fandom.com";
    public static final String PATH = "/wiki/";
    public static final String BASE = WIKI + PATH;

    private static final List<Creator<? extends OWLEntity>> selectorMap = List.of(
            new IndividualCreator(
                    "#app_characters + table + .appearances a, " +
                            "#app_canon_characters + table + .appearances a")
                    .withType("Living_thing"),

            hierarchicalIndividualCreator("#app_locations", "#app_canon_locations", "Place","locatedIn"),
            hierarchicalIndividualCreator("#app_events", "#app_canon_events", "Event","during"),

            hierarchicalClassCreator("#app_vehicles", "#app_canon_vehicles", "Vehicle"),
            hierarchicalClassCreator("#app_creatures", "#app_canon_creatures", "Living_thing"),
            hierarchicalClassCreator("#app_droids","#app_canon_droids", "Droid"),
            hierarchicalClassCreator("#app_species","#app_canon_species", "Living_thing"),
            hierarchicalClassCreator("#app_technology","#app_canon_technology", "Equipment"),
            hierarchicalClassCreator("#app_miscellanea","#app_canon_miscellanea", "Object")
            // TODO organisations and titles are a mixture of inds and classes
    );

    private static ClassCreator hierarchicalClassCreator(String firstRoot, String secondRoot, String baseType) {
        return new ClassCreator("> a")
                .withCommonParent(
                        firstRoot + " + .appearances li, " +
                                secondRoot + " + .appearances li") // needed as we can only select down
                .withType(baseType)
                .withSubclasses("> ul > li > a");
    }

    private static IndividualCreator hierarchicalIndividualCreator(String firstRoot, String secondRoot, String baseType, String relation) {
        return new IndividualCreator("> a")
                .withCommonParent(
                        firstRoot + " + .appearances li, " +
                        secondRoot + " + .appearances li") // needed as we can only select down
                .withType(baseType)
                .withRelation(relation, "> ul > li > a");
    }

    private static final WikiCache fileCache = new WikiCache();

    private final static Map<IRI, WikiPage> cache = new HashMap<>();

    public static WikiPage forIRI(Helper helper, IRI iri) throws IOException {
        cache.putIfAbsent(iri, new WikiPage(helper, fileCache.getFromWebOrCache(iri), selectorMap));
        return cache.get(iri);
    }

    public static WikiPage forName(Helper helper, String ref) throws IOException {
        return forIRI(helper, getWikiUrl(ref));
    }

    public static WikiPage forOWLEntity(Helper helper, OWLEntity entity) throws IOException {
        OWLAnnotationProperty seeAlso = helper.df.getRDFSSeeAlso();
        Optional<IRI> optIRI = getWookieepediaRefsFor(entity, helper, seeAlso);
        return (optIRI.isPresent()) ?
                forIRI(helper, optIRI.get()) :
                forName(helper, helper.render(entity));
    }


    public static WikiPage forString(@Nonnull String ref, Helper helper) throws IOException {
        if (ref.startsWith("http")) {
            return Wiki.forIRI(helper, IRI.create(ref));
        } else {
            OWLAnnotationProperty seeAlso = helper.df.getRDFSSeeAlso();
            List<OWLEntity> entities = FinderUtils.annotationExact(ref, seeAlso, helper);
            if (entities.isEmpty()) {
                return Wiki.forName(helper, ref);
            } else {
                OWLEntity entity = entities.get(0);
                return Wiki.forOWLEntity(helper, entity);
            }
        }
    }

    private static Optional<IRI> getWookieepediaRefsFor(OWLEntity entity, Helper helper, OWLAnnotationProperty seeAlso) {
        return helper.ont.annotationAssertionAxioms(entity.getIRI(), Imports.INCLUDED)
                .filter(ax -> ax.getProperty().equals(seeAlso))
                .map(OWLAnnotationAssertionAxiom::getValue)
                .map(OWLAnnotationValue::asLiteral)
                .flatMap(Optional::stream)
                .map(OWLLiteral::getLiteral)
                .findFirst()
                .map(IRI::create);
    }

    private static IRI getWikiUrl(String ref) {
        return IRI.create(BASE + ref);
    }

    public static List<? extends OWLObject> suggestions() {
        return null;
    }

    public static String pageName(IRI iri) {
        String path = iri.toURI().getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
