package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddAxiomCommand implements Command {

    Logger logger = LoggerFactory.getLogger(AddAxiomCommand.class);

    private final Helper helper;
    private final OWLAnnotationProperty defaultSearchLabel;

    public AddAxiomCommand(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.defaultSearchLabel = defaultSearchLabel;
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return FinderUtils.annotationContains(input.autocompleteWord(), defaultSearchLabel, helper).stream()
                .map(helper::render).collect(Collectors.toList());
    }

    @Override
    public Context handle(UserInput commandStr, Context context) {
        String param = commandStr.paramsAsString();

        OWLOntology targetOntology = context.getOntology(helper);

        try {
            OWLAxiom ax = helper.mosAxiom(param);

            List<OWLOntologyChange> changes = new ArrayList<>();
            changes.add(new AddAxiom(targetOntology, ax));
            logger.debug("Added " + helper.render(ax) + " to " + helper.renderOntology(targetOntology));

            helper.mngr.applyChanges(changes);
        }
        catch (ParserException e) {
            logger.debug(e.getMessage());
            return createPlaceholderContext(commandStr.fullText(), e, context);
        }
        return context;
    }

    private Context createPlaceholderContext(String commandStr, ParserException e, Context context) {
        EntityType type = getExpectedType(e);
        String token = e.getCurrentToken();
        List<OWLEntity> entities = FinderUtils.annotationContains(token, helper.df.getRDFSLabel(), type, helper);
        String s = commandStr.replace(token, "?" + token + "?");
        return new OWLObjectListContext(s, context, entities);
    }

    private EntityType getExpectedType(ParserException e) {
        if (e.isIndividualNameExpected()) {
            return EntityType.NAMED_INDIVIDUAL;
        }
        else if (e.isClassNameExpected()) {
            return EntityType.CLASS;
        }
        else if (e.isObjectPropertyNameExpected()) {
            return EntityType.OBJECT_PROPERTY;
        }
        else if (e.isDataPropertyNameExpected()) {
            return EntityType.DATA_PROPERTY;
        }
        else if (e.isAnnotationPropertyNameExpected()) {
            return EntityType.ANNOTATION_PROPERTY;
        }
        else if (e.isDatatypeNameExpected()) {
            return EntityType.DATATYPE;
        }
        throw new RuntimeException(e.getMessage());
    }
}
