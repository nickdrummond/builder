package com.nickd.wiki.creator;

import com.nickd.builder.Constants;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import java.util.ArrayList;
import java.util.List;

public class EntityBuilder {

    private Helper helper;

    private final OWLAnnotationProperty editorLabel;
    private final OWLAnnotationProperty legacyId;
    private final OWLAnnotationProperty rdfsLabel;
    private final OWLAnnotationProperty seeAlso;
    private final OWLDatatype anyURI;

    public EntityBuilder(Helper helper, OWLAnnotationProperty editorLabel) {
        this.helper = helper;
        this.editorLabel = editorLabel;

        legacyId = helper.annotProp(Constants.LEGACY_ID, Constants.UTIL_BASE);
        rdfsLabel = helper.df.getRDFSLabel();
        seeAlso = helper.df.getRDFSSeeAlso();
        anyURI = helper.df.getOWLDatatype(XSDVocabulary.ANY_URI);
    }


    public OWLNamedIndividual build(OWLClass cls, String editorLabel, IRI seeAlso, OWLOntology targetOntology) {
        String label = fromId(editorLabel);
        OWLNamedIndividual ind = helper.ind(editorLabel);

        List<OWLOntologyChange> changes = new ArrayList<>();

        if (cls != null) {
            changes.add(addType(ind, cls, targetOntology));
        }
        changes.add(addDeclaration(ind, targetOntology));
        changes.add(addLabel(label, ind, targetOntology));
        changes.add(addEditorLabel(editorLabel, ind, targetOntology));
        changes.add(addLegacyId(ind, targetOntology));

        if (seeAlso != null) {
            // TODO else check if there is a ref at WOOKIEEPEDIA_BASE/label
            changes.add(addSeeAlso(seeAlso, ind, targetOntology));
        }

        helper.mngr.applyChanges(changes);

        return ind;
    }


    public OWLClass buildCls(OWLClass superclass, String editorLabel, IRI seeAlso, OWLOntology targetOntology) {
        String label = fromId(editorLabel);
        OWLClass cls = helper.cls(editorLabel);

        List<OWLOntologyChange> changes = new ArrayList<>();

        if (superclass != null) {
            changes.add(addSuperclass(cls, superclass, targetOntology));
        }
        changes.add(addDeclaration(cls, targetOntology));
        changes.add(addLabel(label, cls, targetOntology));
        changes.add(addEditorLabel(editorLabel, cls, targetOntology));
        changes.add(addLegacyId(cls, targetOntology));

        if (seeAlso != null) {
            // TODO else check if there is a ref at WOOKIEEPEDIA_BASE/label
            changes.add(addSeeAlso(seeAlso, cls, targetOntology));
        }

        helper.mngr.applyChanges(changes);

        return cls;
    }

    private OWLOntologyChange addSuperclass(OWLClass cls, OWLClass superclass, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, helper.df.getOWLSubClassOfAxiom(cls, superclass));
    }

    private OWLOntologyChange addType(OWLNamedIndividual ind, OWLClass cls, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, helper.df.getOWLClassAssertionAxiom(cls, ind));
    }

    private AddAxiom addLegacyId(OWLEntity ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(legacyId, ind, helper.lit(Integer.toString(ind.hashCode()))));
    }

    private AddAxiom addDeclaration(OWLEntity ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, helper.df.getOWLDeclarationAxiom(ind));
    }

    private AddAxiom addEditorLabel(String id, OWLEntity ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(editorLabel, ind, helper.lit(id)));
    }

    private AddAxiom addLabel(String label, OWLEntity ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(rdfsLabel, ind, helper.lit(label, Constants.DEFAULT_LANG)));
    }

    private AddAxiom addSeeAlso(IRI iri, OWLEntity ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(seeAlso, ind, helper.lit(iri.getIRIString(), anyURI)));
    }

    private OWLAnnotationAssertionAxiom getAnnotationAxiom(OWLAnnotationProperty prop, OWLEntity ind, OWLLiteral value) {
        return helper.df.getOWLAnnotationAssertionAxiom(prop, ind.getIRI(), value);
    }

    private String toId(String label) {
        return label.replaceAll(" ", "_");
    }

    private String fromId(String label) {
        return label.replaceAll("_", " ");
    }
}
