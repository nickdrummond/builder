package com.nickd.builder;

import com.nickd.util.App;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public interface Context {
    List<? extends OWLObject> getSelectedObjects();

    Context getParent();

    List<Context> stack(int promptDepth);

    List<Context> stack();

    @Nonnull
    String getName();

    OWLObject getSelected();

    void renderSelection(PrintStream out, App app);

    boolean isSingleSelection();

    OWLOntology getOntology();

    Optional<OWLEntity> getOWLEntity();

    Optional<OWLClass> getOWLClass();

    Optional<OWLAxiom> getOWLAxiom();

    boolean isRoot();

    <T extends OWLObject> Optional<T> get(Class<T> c);
}
