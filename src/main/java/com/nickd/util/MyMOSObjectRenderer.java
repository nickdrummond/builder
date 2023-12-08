package com.nickd.util;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import java.io.Writer;
import java.util.Optional;

public class MyMOSObjectRenderer extends ManchesterOWLSyntaxObjectRenderer {
    private final App app;

    public MyMOSObjectRenderer(Writer writer, App app) {
        super(writer, app.sfp);
        this.app = app;
    }

    @Override
    public void visit(IRI iri) {
        // check if the IRI matches an entity in the ontology
        // could check the base of the IRI first as an optimisation
        Optional<OWLEntity> entity = app.entitiesForIRI(iri).findFirst();
        if (entity.isPresent()) {
            entity.get().accept(this);
        }
        else {
            this.write(iri.getRemainder().orElse(iri.getIRIString()));
        }
    }
}
