package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddAxiomCommand implements Command {

    Logger logger = LoggerFactory.getLogger(AddAxiomCommand.class);

    private final App app;
    private final ParserCommon common;

    public AddAxiomCommand(App app, OWLAnnotationProperty defaultSearchLabel) {
        this.app = app;
        this.common = new ParserCommon(app, defaultSearchLabel);
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return common.autocomplete(input, context);
    }

    @Override
    public Context handle(UserInput input, Context context) {

        if (input.fullText().contains("&")) {
            input = replaceVars(input, context);
        }

        String param = input.paramsAsString();

        OWLOntology targetOntology = context.getOntology();

        try {
            logger.warn("parse = {}", param);
            OWLAxiom ax = app.mosAxiom(param);

            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.add(new AddAxiom(targetOntology, ax));
            logger.debug("Added " + app.render(ax) + " to " + app.render(targetOntology));

            app.mngr.applyChanges(changes);

            // If we have referenced entities not currently in the ontology...
            List<OWLEntity> acceptSuggestions = getReferencedSuggestions(ax).toList();
            if (!acceptSuggestions.isEmpty()) {
                System.out.println("You should probably accept the following suggestions:");
                String name = (acceptSuggestions.size() == 1) ? app.render(acceptSuggestions.get(0)) : "To Accept";
                return new OWLObjectListContext(name, context, acceptSuggestions);
            }
            else {
                return context;
            }
        }
        catch (ParserException e) {
            System.err.println(e.getMessage());
            Context placeHolder = common.createPlaceholderContext(input, e, context);
            if (placeHolder.isSingleSelection()) { //exact match
                if (input.fullText().equals(placeHolder.getName())) {
                    System.err.println("Loop detected: " + input.fullText());
                    System.out.println("Match: " + placeHolder.getSelected());
                    return placeHolder;
                }
                return handle(new UserInput(placeHolder.getName()), context); // try to parse again
            }
            else {
                return placeHolder;
            }
        }
    }

    private Stream<OWLEntity> getReferencedSuggestions(OWLAxiom ax) {
        return ax.signature().filter(this::isSuggestion);
    }

    private boolean isSuggestion(OWLEntity owlEntity) {
        return !app.ont.containsEntityInSignature(owlEntity.getIRI(), Imports.INCLUDED);
    }

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(app::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }
}
