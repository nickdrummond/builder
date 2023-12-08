package com.nickd.builder;

import com.nickd.util.App;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RootContext implements Context {
    private final OWLOntology ont;

    public RootContext(OWLOntology ont) {
        this.ont = ont;
    }

    @Override
    public List<? extends OWLObject> getSelectedObjects() {
        return Collections.emptyList();
    }

    @Override
    public Context getParent() {
        return null;
    }

    @Override
    public List<Context> stack(int promptDepth) {
        return stack();
    }

    @Override
    public List<Context> stack() {
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public String getName() {
        return "";
    }

    @Override
    public OWLObject getSelected() {
        return null;
    }

    @Override
    public void renderSelection(PrintStream out, App app) {

    }

    @Override
    public boolean isSingleSelection() {
        return true;
    }

    @Override
    public OWLOntology getOntology() {
        return ont;
    }

    @Override
    public Optional<OWLEntity> getOWLEntity() {
        return Optional.empty();
    }

    @Override
    public Optional<OWLClass> getOWLClass() {
        return Optional.empty();
    }

    @Override
    public Optional<OWLAxiom> getOWLAxiom() {
        return Optional.empty();
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public <T extends OWLObject> Optional<T> get(Class<T> c) {
        return Optional.empty();
    }
}
