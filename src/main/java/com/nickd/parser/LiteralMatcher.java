package com.nickd.parser;

import com.nickd.util.MyStringUtils;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;

import java.util.Collections;

public class LiteralMatcher extends AbstractParseMatcher {

    public static final String DT_MARKER = "^^";
    private OWLLiteral lit;

    @Override
    public OWLLiteral getLiteral() {
        return lit;
    }

    @Override
    public void check(MyTokenizer tokenizer, OWLEntityChecker checker, OWLDataFactory df) throws ParserException {
        String s = tokenizer.consumeNext();

        if (s.startsWith("\"") && s.endsWith("\"")) {

            String value = MyStringUtils.stripQuotes(s);

            if (tokenizer.remainder().startsWith("@")) { // lang literal
                String lang = tokenizer.consumeNext().substring(1);
                this.lit = df.getOWLLiteral(value, lang);
                return;
            }

            if (tokenizer.remainder().startsWith(DT_MARKER)) { // typed literal
                String datatypeS = tokenizer.consumeNext().substring(DT_MARKER.length());
                OWLDatatype dt = checker.getOWLDatatype(datatypeS);
                if (dt != null) {
                    this.lit = df.getOWLLiteral(value, dt);
                    return;
                }
                int pointer = tokenizer.getPointer() + DT_MARKER.length();
                throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, false, false, true, false, false, Collections.emptySet());
            }

            this.lit = df.getOWLLiteral(value);
            return;
        }

        if (s.equals("true")) {
            this.lit = df.getOWLLiteral(true);
            return;
        } else if (s.equals("false")) {
            this.lit = df.getOWLLiteral(false);
            return;
        }

        try {
            int i = Integer.parseInt(s);
            this.lit = df.getOWLLiteral(i); // integer literal
            return;
        } catch (NumberFormatException e) {
            // not an int
        }
        try {
            float f = Float.parseFloat(s);
            this.lit = df.getOWLLiteral(f);
            return;
        } catch (NumberFormatException ef) {
            // not a float
        }
        try {
            double d = Double.parseDouble(s);
            this.lit = df.getOWLLiteral(d);
            return;
        } catch (NumberFormatException ef) {
            // not a double
        }

        int pointer = tokenizer.getPointer();
        throw new ParserException(tokenizer.tokens(), pointer, 0, pointer, false, false, false, false, false, false, false, false, Collections.emptySet());
    }
}
