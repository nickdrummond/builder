package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
        String param = input.paramsAsString();

        OWLOntology targetOntology = context.getOntology(helper);

        try {
            OWLAxiom ax = helper.mosAxiom(param);

            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.add(new AddAxiom(targetOntology, ax));
            logger.debug("Added " + helper.render(ax) + " to " + helper.render(targetOntology));

            helper.mngr.applyChanges(changes);
            return new OWLObjectListContext(helper.render(ax), context, ax);

        }
        catch (ParserException e) {
            logger.debug(e.getMessage());
            Context placeHolder = common.createPlaceholderContext(input.fullText(), e, context);
            if (placeHolder.isSingleSelection()) { //exact match
                return handle(new UserInput(placeHolder.getName()), context); // try to parse again
            }
            else {
                return placeHolder;
            }
        }
    }
}
