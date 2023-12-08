package com.nickd.builder.command;

import com.nickd.builder.Constants;
import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.DescriptionVisitorEx;
import com.nickd.util.App;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class AcceptCommand implements Command {

    Logger logger = LoggerFactory.getLogger(AcceptCommand.class);

    private final App app;
    private final ParserCommon common;

    public AcceptCommand(App app, OWLAnnotationProperty defaultSearchLabel) {
        this.app = app;
        this.common = new ParserCommon(app, defaultSearchLabel);
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return common.autocomplete(input, context);
    }

    public Context handle(UserInput input, Context context) {
        Set<OWLObject> sel = new HashSet<>();
        if (!input.params().isEmpty()) { // this should be &1 and we get the axiom directly from the context
            String param = input.params().get(0);
            if (param.startsWith("&")) {
                sel.add(context.getSelectedObjects().get(Integer.parseInt(param.substring(1))));
            } else if (param.equals("all")) {
                sel.addAll(context.getSelectedObjects());
            }
            else {
                sel.add(app.mos(param));
            }
        } else if (context.isSingleSelection()) {
            sel.add(context.getSelected());
        }

        List<OWLAxiomChange> changes = sel.stream().filter(OWLObject::isNamed).flatMap(entity -> {
            final OWLOntology target = (entity instanceof OWLNamedIndividual) ?
                    app.ont(Constants.DEFAULT_INDIVIDUALS_ONT) :
                    app.ont(Constants.DEFAULT_CLASSES_ONT);
            return getChanges((OWLEntity) entity, app.suggestions, target);
        }).toList();
        app.mngr.applyChanges(changes);
        logger.info("Moved {} axioms", changes.size()/2);
        return context; // stay in current context
    }

    private Stream<OWLAxiomChange> getChanges(OWLEntity sel, OWLOntology from, OWLOntology to) {
        List<OWLAxiom> axioms = sel.accept(new DescriptionVisitorEx(from));
        return Stream.concat(
                axioms.stream().map(a -> new AddAxiom(to, a)),
                axioms.stream().map(a -> new RemoveAxiom(from, a)));
    }
}
