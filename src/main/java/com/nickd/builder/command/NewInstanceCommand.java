package com.nickd.builder.command;

import com.nickd.builder.*;
import com.nickd.util.Helper;
import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewInstanceCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(NewInstanceCommand.class);

    private final OWLAnnotationProperty editorLabel;
    private final OWLAnnotationProperty legacyId;
    private final OWLAnnotationProperty rdfsLabel;
    private final OWLAnnotationProperty seeAlso;
    private final OWLDatatype anyURI;
    private final Helper helper;

    public NewInstanceCommand(Helper helper, OWLAnnotationProperty editorLabel) {
        this.helper = helper;

        this.editorLabel = editorLabel;
        legacyId = helper.annotProp(Constants.LEGACY_ID, Constants.UTIL_BASE);
        rdfsLabel = helper.df.getRDFSLabel();
        seeAlso = helper.df.getRDFSSeeAlso();
        anyURI = helper.df.getOWLDatatype(XSDVocabulary.ANY_URI);
    }


    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Create a new individual with some paramsAsString");
    }

    @Override
    public Context handle(UserInput input, Context context) {

        // TODO this is going to have to go :(
        // As much as this is nice and "cheap" for helping out, all we're doing is creating strings which have to be
        // pulled apart again by each command - when they could just get the object from parent context!!
        // Good for entities, not so good for axioms etc
        if (input.fullText().contains("&")) {
            input = replaceVars(input, context);
        }

        List<String> params = input.params();

        if (params.size() >= 3) {
            String type = params.get(0);
            OWLClass cls = helper.cls(type);

            if (!helper.ont.containsClassInSignature(cls.getIRI(), Imports.INCLUDED)) {
                logger.warn("No class found in ontologies: " + cls);
                return context;
            }

            String label = params.get(1);
            String id = toId(label);
            OWLNamedIndividual ind = helper.ind(id);

            if (helper.ont.containsEntityInSignature(ind.getIRI(), Imports.INCLUDED)) {
                logger.warn("IRI already used in ontologies: " + ind);
                return context;
            }

            OWLOntology targetOntology = context.getOntology(helper);

            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.add(addDeclaration(ind, targetOntology));
            changes.add(addType(ind, cls, targetOntology));
            changes.add(addLabel(label, ind, targetOntology));
            changes.add(addEditorLabel(id, ind, targetOntology));
            changes.add(addLegacyId(ind, targetOntology));

            if (params.size() == 3) {
                changes.add(addSeeAlso(params.get(2), ind, targetOntology));
            }
            // TODO else check if there is a ref at WOOKIEEPEDIA_BASE/label

            helper.mngr.applyChanges(changes);

            return new OWLObjectListContext(helper.render(ind), context, ind);
        }
        return context;
    }

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(helper::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }

    private OWLOntologyChange addType(OWLNamedIndividual ind, OWLClass cls, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, helper.df.getOWLClassAssertionAxiom(cls, ind));
    }

    private AddAxiom addLegacyId(OWLNamedIndividual ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(legacyId, ind, helper.lit(Integer.toString(ind.hashCode()))));
    }

    private AddAxiom addDeclaration(OWLNamedIndividual ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, helper.df.getOWLDeclarationAxiom(ind));
    }

    private AddAxiom addEditorLabel(String id, OWLNamedIndividual ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(editorLabel, ind, helper.lit(id)));
    }

    private AddAxiom addLabel(String label, OWLNamedIndividual ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(rdfsLabel, ind, helper.lit(label, Constants.DEFAULT_LANG)));
    }

    private AddAxiom addSeeAlso(String url, OWLNamedIndividual ind, OWLOntology targetOntology) {
        return new AddAxiom(targetOntology, getAnnotationAxiom(seeAlso, ind, helper.lit(url, anyURI)));
    }

    private OWLAnnotationAssertionAxiom getAnnotationAxiom(OWLAnnotationProperty prop, OWLNamedIndividual ind, OWLLiteral value) {
        return helper.df.getOWLAnnotationAssertionAxiom(prop, ind.getIRI(), value);
    }

    private String toId(String label) {
        return label.replaceAll(" ", "_");
    }
}
