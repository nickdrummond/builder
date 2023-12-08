package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import com.nickd.util.FinderUtils;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParserCommon {

    Logger logger = LoggerFactory.getLogger(ParserCommon.class);

    private final Helper helper;
    private final OWLAnnotationProperty defaultSearchLabel;

    public ParserCommon(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.defaultSearchLabel = defaultSearchLabel;
    }

    Context createPlaceholderContext(UserInput input, ParserException e, Context context) {
        String token = e.getCurrentToken();
        List<OWLEntity> entities = getExpectedType(e);
        String remains = input.paramsAsString().substring(e.getStartPos());
        String s = input.paramsAsString().substring(0, e.getStartPos());
        if (entities.size() > 1) {
            if (remains.isEmpty()) {
                s += " ??";
            }
            else {
                s += remains.replace(token, "?" + token + "?");
            }
        }
        else if (entities.size() == 1) {
            if (remains.isEmpty()) {
                s += " " + helper.render(entities.get(0));
            }
            else {
                s += remains.replace(token, helper.render(entities.get(0)));
            }
        }
        else {
            if (remains.isEmpty()) {
                s += " ??";
            }
            else {
                s += remains.replace(token, "??");
            }
        }
        return new OWLObjectListContext(input.command() + " " + s, context, entities);
    }

    // TODO should parse the input at the ? and restrict the autocomplete to the correct type/keyword
    public List<String> autocomplete(UserInput input, Context context) {
        return FinderUtils.annotationContains(input.autocompleteWord(), defaultSearchLabel, helper).stream()
                .map(helper::render).collect(Collectors.toList());
    }

    private List<OWLEntity> getExpectedType(ParserException e) {
        String token = e.getCurrentToken();

        List<OWLEntity> expected = new ArrayList<>();
        if (e.isIndividualNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.NAMED_INDIVIDUAL, helper));
        }

        if (e.isClassNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.CLASS, helper));
        }

        if (e.isObjectPropertyNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.OBJECT_PROPERTY, helper));
        }

        if (e.isDataPropertyNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.DATA_PROPERTY, helper));
        }

        if (e.isAnnotationPropertyNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.ANNOTATION_PROPERTY, helper));
        }

        if (e.isDatatypeNameExpected()) {
            expected.addAll(FinderUtils.annotationContains(token, defaultSearchLabel, EntityType.DATATYPE, helper));
        }

        return expected;
    }
}
