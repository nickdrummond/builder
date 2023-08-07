package com.nickd.parser;

import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Collections;

public class QuotedStringMatcher extends AbstractParseMatcher<String> {

    private String value;

    @Override
    public String get() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        String s = tokenizer.consumeNext();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            this.value = MyStringUtils.stripQuotes(s);
        }
        else {
            int pointer = tokenizer.getPointer();
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, false, false, false, false,false, Collections.emptySet());
        }
    }
}
