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
        int pointer = tokenizer.getPointer()+1;
        OWLDataProperty prop = checker.getOWLDataProperty(tokenizer.consumeNext());
        if (prop == null) {
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer+1, false, false, false, true, false, false, false, false, Collections.emptySet());
        }
        this.prop = prop;
    }
}
