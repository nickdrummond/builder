package com.nickd.wiki.creator;

import com.nickd.builder.Constants;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

public class IndividualCreator implements Creator<OWLNamedIndividual> {
    private final String type;

    public IndividualCreator(String type) {
        this.type = type;
    }

    @Override
    public OWLNamedIndividual create(String name, IRI iri, Helper helper) {
        OWLAnnotationProperty editorLabel = helper.annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        OWLClass rootType = helper.cls(type);
        EntityBuilder entityBuilder = new EntityBuilder(helper, editorLabel);
        return entityBuilder.build(rootType, name, iri, helper.suggestions);
    }
}
