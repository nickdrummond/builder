package com.nickd.parser;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Collections;

import static org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax.INVERSE;

public class ObjPropExpressionMatcher extends AbstractParseMatcher<OWLObjectPropertyExpression> {
    private OWLObjectPropertyExpression expr;

    @Override
    public OWLObjectPropertyExpression get() {
        return expr;
    }

    @Override
    public OWLObjectPropertyExpression getObjectPropertyExpression() {
        return expr;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {

        boolean inv = false;
        OWLObjectProperty prop;
        String s = tokenizer.consumeNext();
        if (s.equals(INVERSE.keyword())) {
            inv = true;
            prop = checker.getOWLObjectProperty(tokenizer.consumeNext());
        }
        else {
            prop = checker.getOWLObjectProperty(s);
        }

        if (prop == null) {
            int pointer = tokenizer.getPointer();
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, true, false, false, false, false, false, Collections.emptySet());
        }
        this.expr = inv ? df.getOWLObjectInverseOf(prop) : prop;
    }
}
