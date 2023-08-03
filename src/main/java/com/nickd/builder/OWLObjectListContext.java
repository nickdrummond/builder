package com.nickd.builder;

import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OWLObjectListContext extends ContextBase {

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
    public String toString(Helper helper) {
        int size = selectedObjects.size();
        return (size == 1) ? renderFirst(helper) : getName() + " (" + size + ")";
    }

    @Override
    public String renderFirst(Helper helper) {
        OWLObject o = getSelected();
        if (o instanceof OWLOntology) {
            return helper.renderOntology((OWLOntology) o);
        }
        else {
            return helper.render(o);
        }
    }

    @Override
    public OWLObject getSelected() {
        return selectedObjects.get(0);
    }

    @Override
    public void describe(PrintStream out, Helper helper) {
        if (!isSingleSelection()) {
            for (int i = 0; i < selectedObjects.size(); i++) {
                OWLObject o = selectedObjects.get(i);
                out.println("\t" + i + ") " + ((o instanceof OWLOntology)
                        ? helper.renderOntology((OWLOntology) o)
                        : helper.render(o)));
            }
        }
    }

    @Override
    public boolean isSingleSelection() {
        return selectedObjects.size() == 1;
    }

    @Override
    public OWLOntology getOntology(Helper helper) {
        if (isSingleSelection()) {
            OWLObject o = getSelected();
            if (o instanceof OWLOntology) {
                return (OWLOntology) o;
            }
        }
        else if (isRoot()) {
            return helper.ont; // root
        }

        return parent.getOntology(helper); // check parents
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
        if (isSingleSelection()) {
            OWLObject o = getSelected();
            if (o instanceof OWLClass) {
                return Optional.of((OWLClass) o);
            }
        }
        else if (isRoot()) {
            return Optional.empty();
        }

        return parent.getOWLClass();
    }

}
