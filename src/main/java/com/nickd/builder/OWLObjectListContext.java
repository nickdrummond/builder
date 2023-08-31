package com.nickd.builder;

import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OWLObjectListContext extends AbstractContext {

    private final List<? extends OWLObject> selectedObjects;

    public OWLObjectListContext(@Nonnull String name, @Nonnull Context parent, List<? extends OWLObject> selectedObjects) {
        super(name, parent);
        this.selectedObjects = selectedObjects;
    }

    public OWLObjectListContext(@Nonnull String name, @Nonnull Context parent, OWLObject owlObject) {
        this(name, parent, Collections.singletonList(owlObject));
    }

    public OWLObjectListContext() {
        this("", null, Collections.emptyList());
    }

    @Override
    public List<? extends OWLObject> getSelectedObjects() {
        return selectedObjects;
    }

    @Override
    public OWLObject getSelected() {
        return selectedObjects.get(0);
    }

    @Override
    public void renderSelection(PrintStream out, Helper helper) {
        if (!isSingleSelection()) {
            for (int i = 0; i < selectedObjects.size(); i++) {
                OWLObject o = selectedObjects.get(i);
                out.println("\t" + i + ") " + helper.render(o));
            }
        }
    }

    @Override
    public boolean isSingleSelection() {
        return selectedObjects.size() == 1;
    }

    @Override
    public OWLOntology getOntology() {
        if (isSingleSelection()) {
            OWLObject o = getSelected();
            if (o instanceof OWLOntology) {
                return (OWLOntology) o;
            }
        }

        return parent.getOntology(); // check parents
    }


    @Override
    public Optional<OWLEntity> getOWLEntity() {
        if (isSingleSelection()) {
            OWLObject o = getSelected();
            if (o instanceof OWLEntity) {
                return Optional.of((OWLEntity) o);
            }
        }

        return (isRoot())  ? Optional.empty() : parent.getOWLEntity();
    }

    @Override
    public Optional<OWLClass> getOWLClass() {
        return get(OWLClass.class);
    }

    @Override
    public Optional<OWLAxiom> getOWLAxiom() {
        return get(OWLAxiom.class);
    }

    @Override
    public <T extends OWLObject> Optional<T> get(Class<T> cls) {
        if (isRoot()) {
            return Optional.empty();
        }
        else if (isSingleSelection()) {
            OWLObject o = getSelected();
            if (o != null && cls.isAssignableFrom(o.getClass())) {
                return Optional.of((T)o);
            }
        }

        return parent.get(cls);
    }
}
