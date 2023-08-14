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
    public static final String WIKI = "https://starwars.fandom.com";
    public static final String PATH = "/wiki/";
    public static final String BASE = WIKI + PATH;

    private static List<Creator> selectorMap = List.of(
            new IndividualCreator("#app_characters + table + .appearances a")
                    .withType("Living_Thing"),
            new IndividualCreator("> a")
                    .withCommonParent("#app_locations + .appearances li") // needed as we can only select down
                    .withType("Place")
                    .withRelation("locatedIn", "> ul > li > a"),
            new ClassCreator("> a")
                    .withCommonParent("#app_vehicles + .appearances li") // needed as we can only select down
                    .withType("Vehicle")
                    .withSubclasses("> ul > li > a")

    );

    private final static Map<IRI, WikiPage> cache = new HashMap<>();

    public static WikiPage forIRI(Helper helper, IRI iri) throws IOException {
        cache.putIfAbsent(iri, new WikiPage(helper, iri, selectorMap));
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
}
