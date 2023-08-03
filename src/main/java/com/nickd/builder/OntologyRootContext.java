package com.nickd.builder;

import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.PrintStream;

public class OntologyRootContext extends OWLObjectListContext {
    private final OWLOntology ont;

    public OntologyRootContext(OWLOntology ont) {
        super();
        this.ont = ont;
    }

    @Override
    public boolean isSingleSelection() {
        return true;
    }

    @Override
    public OWLOntology getOntology(Helper helper) {
        return ont;
    }

    @Override
    public void describe(PrintStream out, Helper helper) {
        // do nothing
    }

    @Override
    public boolean isRoot() {
        return true;
    }
}
