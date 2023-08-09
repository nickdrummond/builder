package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import com.nickd.parser.matcher.AbstractParseMatcher;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;

import java.util.Collections;

public class DataPropMatcher extends AbstractParseMatcher<OWLDataProperty> {

    private OWLDataProperty prop;

    @Override
    public OWLDataProperty get() {
        return prop;
    }

    @Override
    public OWLDataProperty getDataProperty() {
        return prop;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        OWLDataProperty prop = checker.getOWLDataProperty(tokenizer.consumeNext());
        if (prop == null) {
            int pointer = tokenizer.getPointer();
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, true, false, false, false, false, Collections.emptySet());
        }
        this.prop = prop;
    }
}
