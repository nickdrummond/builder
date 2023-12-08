package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.FinderUtils;
import com.nickd.util.App;
import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoveAxiomCommand implements Command {

    Logger logger = LoggerFactory.getLogger(MoveAxiomCommand.class);

    private final App app;
    private final ParserCommon common;

    public MoveAxiomCommand(App app, OWLAnnotationProperty defaultSearchLabel) {
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

        Optional<OWLAxiom> ax = Optional.empty();

        if (!input.isEmpty()) {
            try {
                String param = input.paramsAsString();
                ax = Optional.of(app.mosAxiom(param));
            } catch (ParserException e) {
                logger.error("Cannot find axiom", e);
                return common.createPlaceholderContext(input, e, context);
            }
        }
        if (ax.isEmpty()) {
            ax = context.getOWLAxiom();
        }

        OWLOntology to = context.getOntology();
        ax.ifPresent(a -> move(a, to));

        return context;
    }

    private void move(OWLAxiom ax,  OWLOntology to) {
        List<? extends OWLOntologyChange> changes = FinderUtils.getOntologiesContaining(ax, app.ont)
                .peek(o -> System.out.println("Moving " + app.render(ax) + " from " + app.render(o)))
                .map(o -> List.of(new RemoveAxiom(o, ax), new AddAxiom(to, ax)))
                .flatMap(Collection::stream).collect(Collectors.toList());
        app.mngr.applyChanges(changes);
}

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(app::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }
}
