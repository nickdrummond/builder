package com.nickd.util;

import com.nickd.builder.Constants;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Wiki {

    private final static Map<IRI, WikiPage> cache = new HashMap<>();

    public static WikiPage forIRI(Helper helper, IRI iri) throws IOException {
        cache.putIfAbsent(iri, new WikiPage(helper, iri));
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
        return IRI.create(Constants.WOOKIEEPEDIA_BASE + ref);
    }
}
