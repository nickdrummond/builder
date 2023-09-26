package com.nickd.util;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.io.Writer;
import java.util.Optional;

public class MyMOSObjectRenderer extends ManchesterOWLSyntaxObjectRenderer {
    private final Helper helper;

    public MyMOSObjectRenderer(Writer writer, Helper helper) {
        super(writer, helper.sfp);
        this.helper = helper;
    }

    @Override
    public void visit(IRI iri) {
        // check if the IRI matches an entity in the ontology
        // could check the base of the IRI first as an optimisation
        Optional<OWLEntity> entity = helper.entitiesForIRI(iri).findFirst();
        if (entity.isPresent()) {
            entity.get().accept(this);
        }
        else {
            this.write(iri.getRemainder().orElse(iri.getIRIString()));
        }
    }
}
