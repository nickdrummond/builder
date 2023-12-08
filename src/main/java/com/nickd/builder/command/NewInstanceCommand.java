package com.nickd.builder.command;

import com.nickd.builder.*;
import com.nickd.util.App;
import com.nickd.wiki.creator.EntityBuilder;
import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class NewInstanceCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(NewInstanceCommand.class);

    private final App app;

    private final EntityBuilder entityBuilder;

    public NewInstanceCommand(App app, OWLAnnotationProperty editorLabel) {
        this.app = app;
        entityBuilder = new EntityBuilder(app, editorLabel);
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
            OWLClass cls = app.cls(type);

            if (!app.ont.containsClassInSignature(cls.getIRI(), Imports.INCLUDED)) {
                logger.warn("No class found in ontologies: " + cls);
                return context;
            }

            String label = params.get(1);

//            if (app.ont.containsEntityInSignature(ind.getIRI(), Imports.INCLUDED)) {
//                logger.warn("IRI already used in ontologies: " + ind);
//                return context;
//            }

            OWLOntology targetOntology = context.getOntology();

            String seeAlso = null;
            if (params.size() == 3) {
                seeAlso = params.get(2);
            }

            OWLNamedIndividual ind = entityBuilder.build(cls, label, IRI.create(seeAlso), targetOntology);

            return new OWLObjectListContext(app.render(ind), context, ind);
        }
        return context;
    }

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(app::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }

}
