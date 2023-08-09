package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParserCommon {

    Logger logger = LoggerFactory.getLogger(ParserCommon.class);

    private final Helper helper;
    private final OWLAnnotationProperty defaultSearchLabel;

    public ParserCommon(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.defaultSearchLabel = defaultSearchLabel;
    }

    Context createPlaceholderContext(String commandStr, ParserException e, Context context) {
        String token = e.getCurrentToken();
        List<OWLEntity> entities = getExpectedType(e);
        String s = commandStr.replace(token, "?" + token + "?");
        return new OWLObjectListContext(s, context, entities);
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
