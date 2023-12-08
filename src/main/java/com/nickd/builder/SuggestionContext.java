package com.nickd.builder;

import com.nickd.util.App;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public class SuggestionContext implements Context {

    private Context delegate;
    private OWLOntology suggestions;

    public SuggestionContext(OWLOntology suggestions, Context delegate) {
        this.suggestions = suggestions;
        this.delegate = delegate;
    }

    @Override
    public List<? extends OWLObject> getSelectedObjects() {
        return delegate.getSelectedObjects();
    }

    @Override
    public Context getParent() {
        return delegate.getParent();
    }

    @Override
    public List<Context> stack(int promptDepth) {
        return delegate.stack(promptDepth);
    }

    @Override
    public List<Context> stack() {
        return delegate.stack();
    }

    @Override
    @Nonnull
    public String getName() {
        return delegate.getName();
    }

    @Override
    public OWLObject getSelected() {
        return delegate.getSelected();
    }

    @Override
    public void renderSelection(PrintStream out, App app) {
        delegate.renderSelection(out, app);
    }

    @Override
    public boolean isSingleSelection() {
        return delegate.isSingleSelection();
    }

    @Override
    public OWLOntology getOntology() {
        return suggestions;
    }

    @Override
    public Optional<OWLEntity> getOWLEntity() {
        return delegate.getOWLEntity();
    }

    @Override
    public Optional<OWLClass> getOWLClass() {
        return delegate.getOWLClass();
    }

    @Override
    public Optional<OWLAxiom> getOWLAxiom() {
        return delegate.getOWLAxiom();
    }

    @Override
    public boolean isRoot() {
        return delegate.isRoot();
    }

    @Override
    public <T extends OWLObject> Optional<T> get(Class<T> c) {
        return delegate.get(c);
    }
}
