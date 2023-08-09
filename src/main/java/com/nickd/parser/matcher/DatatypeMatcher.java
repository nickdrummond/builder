package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import com.nickd.parser.matcher.AbstractParseMatcher;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;

import java.util.Collections;

public class DatatypeMatcher extends AbstractParseMatcher<OWLDatatype> {

    private OWLDatatype dt;

    @Override
    public OWLDatatype get() {
        return dt;
    }

    @Override
    public OWLDatatype getDatatype() {
        return dt;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        String s = tokenizer.consumeNext();

        OWLDatatype dt = checker.getOWLDatatype(s);
        if (dt != null) {
            this.dt = df.getOWLDatatype(dt);
            return;
        }
        int pointer = tokenizer.getPointer();
        throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, false, false, true, false, false, Collections.emptySet());
    }
}
