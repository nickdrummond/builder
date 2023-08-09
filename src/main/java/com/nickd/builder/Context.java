package com.nickd.builder;

import com.nickd.util.Helper;
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

    void renderSelection(PrintStream out, Helper helper);

    boolean isSingleSelection();

    OWLOntology getOntology(Helper helper);

    Optional<OWLEntity> getOWLEntity();

    Optional<OWLClass> getOWLClass();

    Optional<OWLAxiom> getOWLAxiom();

    boolean isRoot();

    <T extends OWLObject> Optional<T> get(Class<T> c);
}
