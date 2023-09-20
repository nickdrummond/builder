package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AddAxiomCommand implements Command {

    Logger logger = LoggerFactory.getLogger(AddAxiomCommand.class);

    private final Helper helper;
    private final ParserCommon common;

    public AddAxiomCommand(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.common = new ParserCommon(helper, defaultSearchLabel);
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
            OWLAxiom ax = helper.mosAxiom(param);

            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.add(new AddAxiom(targetOntology, ax));
            logger.debug("Added " + helper.render(ax) + " to " + helper.render(targetOntology));

            helper.mngr.applyChanges(changes);

            // If we have referenced entities not currently in the ontology...
            List<OWLEntity> acceptSuggestions = getReferencedSuggestions(ax).toList();
            if (!acceptSuggestions.isEmpty()) {
                System.out.println("You should probably accept the following suggestions:");
                String name = (acceptSuggestions.size() == 1) ? helper.render(acceptSuggestions.get(0)) : "To Accept";
                return new OWLObjectListContext(name, context, acceptSuggestions);
            }
            else {
                return context;
            }
        }
        catch (ParserException e) {
            Context placeHolder = common.createPlaceholderContext(input.fullText(), e, context);
            if (placeHolder.isSingleSelection()) { //exact match
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
        return helper.suggestions.containsEntityInSignature(owlEntity.getIRI());
    }

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(helper::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }
}
