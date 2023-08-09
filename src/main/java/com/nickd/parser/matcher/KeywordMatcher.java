package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import com.nickd.parser.matcher.AbstractParseMatcher;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.Set;

public class KeywordMatcher extends AbstractParseMatcher<String> {
    private final String syntax;

    @Override
    public String get() {
        return syntax;
    }

    public KeywordMatcher(ManchesterOWLSyntax syntax) {
        this.syntax = syntax.toString();
    }

    public KeywordMatcher(ManchesterOWLSyntax syntax, String sugar) {
        this.syntax = syntax.toString() + sugar;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        if (!tokenizer.consumeNext().equalsIgnoreCase(syntax)) {
            int pointer = tokenizer.getPointer();
            throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, false, false, false, false, false, Set.of(syntax));
        }
    }
}
