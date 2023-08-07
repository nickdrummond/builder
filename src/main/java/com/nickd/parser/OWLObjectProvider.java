package com.nickd.parser;

import org.semanticweb.owlapi.model.*;

public interface OWLObjectProvider {

    OWLObjectPropertyExpression objProp(String key);

    OWLDataPropertyExpression dataProp(String key);

    OWLAnnotationProperty annotProp(String key);

    OWLIndividual ind(String key);

    OWLClassExpression cls(String key);

    OWLLiteral lit(String key);

    OWLClassExpression clsExpr(String key);
}
