package com.nickd.builder;

import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

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

    String toString(Helper helper);

    String renderFirst(Helper helper);

    OWLObject getSelected();

    void describe(PrintStream out, Helper helper);

    boolean isSingleSelection();

    OWLOntology getOntology(Helper helper);

    Optional<OWLEntity> getOWLEntity();

    Optional<OWLClass> getOWLClass();

    boolean isRoot();
}
