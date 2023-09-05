package com.nickd.builder.command;

import com.nickd.builder.Constants;
import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.DescriptionVisitorEx;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AcceptCommand implements Command {

    Logger logger = LoggerFactory.getLogger(AcceptCommand.class);

    private final Helper helper;
    private final ParserCommon common;

    public AcceptCommand(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.common = new ParserCommon(helper, defaultSearchLabel);
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return common.autocomplete(input, context);
    }

    public Context handle(UserInput input, Context context) {
        OWLObject sel = null;
        if (!input.params().isEmpty()) { // this should be &1 and we get the axiom directly from the context
            String param = input.params().get(0);
            if (param.startsWith("&")) {
                sel = context.getSelectedObjects().get(Integer.parseInt(param.substring(1)));
            } else {
                sel = helper.mos(param);
            }
        } else if (context.isSingleSelection()) {
            sel = context.getSelected();
        }

        if (sel != null && sel.isNamed()) {
            final OWLOntology target = (sel instanceof OWLNamedIndividual) ?
                    helper.ont(Constants.DEFAULT_INDIVIDUALS_ONT) :
                    helper.ont(Constants.DEFAULT_CLASSES_ONT);
            moveAxioms((OWLEntity)sel, helper.suggestions, target);
        }
        return context; // stay in current context
    }

    private void moveAxioms(OWLEntity sel, OWLOntology from, OWLOntology to) {
        List<OWLAxiom> axioms = sel.accept(new DescriptionVisitorEx(from));
        helper.mngr.applyChanges(Stream.concat(
                axioms.stream().map(a -> new AddAxiom(to, a)),
                axioms.stream().map(a -> new RemoveAxiom(from, a))).toList());
        logger.info("Copied {} axioms to {}", axioms.size(), helper.render(to));
    }
}
