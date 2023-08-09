package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import com.nickd.parser.matcher.AbstractParseMatcher;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Collections;

public class ClassMatcher extends AbstractParseMatcher<OWLClass> {
    private OWLClass cls;

    @Override
    public OWLClass get() {
        return cls;
    }

    @Override
    public OWLClass getOWLClass() {
        return cls;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        OWLClass cls = checker.getOWLClass(tokenizer.consumeNext());
        if (cls == null) {
            int pointer = tokenizer.getPointer();
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, true, false, false, false, false, false, false, Collections.emptySet());
        }
        this.cls = cls;
    }
}
