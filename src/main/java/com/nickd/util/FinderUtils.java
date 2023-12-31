package com.nickd.util;

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FinderUtils {

    public static List<OWLEntity> annotationExact(@Nonnull String value,
                                                  @Nonnull OWLAnnotationProperty searchProp,
                                                  @Nonnull App app) {
        return annotationMatchesPredicate(searchProp, app, ax -> literalValueEquals(ax, value));
    }

    public static List<OWLEntity> annotationContains(@Nonnull String value,
                                                     @Nonnull OWLAnnotationProperty searchProp,
                                                     @Nonnull App app) {
        return annotationMatchesPredicate(searchProp, app, ax -> literalValueContains(ax, value));
    }

    public static List<OWLEntity> annotationContains(@Nonnull String value,
                                                     @Nonnull OWLAnnotationProperty searchProp,
                                                     @Nonnull EntityType type,
                                                     @Nonnull App app) {
        return annotationContains(value, searchProp, app).stream()
                .filter(entity -> entity.isType(type))
                .collect(Collectors.toList());
    }

    public static List<OWLEntity> annotationMatchesPredicate(@Nonnull OWLAnnotationProperty searchProp,
                                                             @Nonnull App app,
                                                             @Nonnull Predicate<OWLAnnotationAssertionAxiom> test) {

        return app.mngr.ontologies()
                .flatMap(o -> o.axioms(AxiomType.ANNOTATION_ASSERTION))
                .filter(ax -> ax.getProperty().equals(searchProp))
                .filter(test)
                .map(OWLAnnotationAssertionAxiom::getSubject)
                .map(OWLAnnotationObject::asIRI)
                .flatMap(Optional::stream)
                .flatMap(app::entitiesForIRI)
                .toList();
    }

    private static boolean literalValueContains(OWLAnnotationAssertionAxiom ax, String value) {
        OWLAnnotationValue v = ax.getValue();
        return (v.isLiteral() && v.asLiteral().isPresent()
                && v.asLiteral().get().getLiteral().contains(value));
    }

    private static boolean literalValueEquals(OWLAnnotationAssertionAxiom ax, String value) {
        OWLAnnotationValue v = ax.getValue();
        return (v.isLiteral() && v.asLiteral().isPresent() && v.asLiteral().get().getLiteral().equals(value));
    }

    public static Stream<OWLOntology> getOntologiesContaining(OWLAxiom ax, OWLOntology root) {
        return root.importsClosure().filter(o -> o.containsAxiom(ax, false));
    }
}
