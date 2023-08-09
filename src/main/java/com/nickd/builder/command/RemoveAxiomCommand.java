package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RemoveAxiomCommand implements Command {

    Logger logger = LoggerFactory.getLogger(RemoveAxiomCommand.class);

    private final Helper helper;
    private final ParserCommon common;

    public RemoveAxiomCommand(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.common = new ParserCommon(helper, defaultSearchLabel);
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return common.autocomplete(input, context);
    }

    @Override
    public Context handle(UserInput input, Context context) {

        OWLOntology targetOntology = context.getOntology(helper);

        Optional<OWLAxiom> ax = context.getOWLAxiom();

        if (ax.isEmpty()) {
            try {
                String param = input.paramsAsString();
                ax = Optional.of(helper.mosAxiom(param));
            } catch (ParserException e) {
                logger.debug(e.getMessage());
                return common.createPlaceholderContext(input.fullText(), e, context);
            }
        }

        ax.ifPresent( a -> remove(targetOntology, a));

        return context;
    }

    private void remove(OWLOntology targetOntology, OWLAxiom a) {
        System.out.println("Removing " + helper.render(a));
        helper.mngr.applyChanges(new RemoveAxiom(targetOntology, a));
    }
}
