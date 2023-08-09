package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import com.nickd.parser.matcher.LiteralMatcher;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiteralMatcherTest extends TestCase {

    @Mock
    private OWLEntityChecker checker;

    private final OWLDataFactory df = new OWLDataFactoryImpl();

    @Test
    public void shouldMatchTypedLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLDatatype dt = df.getOWLDatatype(XSDVocabulary.ANY_URI.getIRI());
        OWLLiteral expected = df.getOWLLiteral("value", dt);

        when(checker.getOWLDatatype(eq("anyURI"))).thenReturn(dt);

        matcher.check(new MyTokenizer("\"value\"^^anyURI"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchLangLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral("value", "en");

        matcher.check(new MyTokenizer("\"value\"@en"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchSimpleLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral("value");

        matcher.check(new MyTokenizer("\"value\""), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchIntegerLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral(104);

        matcher.check(new MyTokenizer("104"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchFloatLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral(104.5f);

        matcher.check(new MyTokenizer("104.5"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchBooleanTrueLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral(true);

        matcher.check(new MyTokenizer("true"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test
    public void shouldMatchBooleanFalseLiteral() {
        LiteralMatcher matcher = new LiteralMatcher();

        OWLLiteral expected = df.getOWLLiteral(false);

        matcher.check(new MyTokenizer("false"), checker, df);

        assertEquals(expected, matcher.getLiteral());
    }


    @Test(expected = ParserException.class)
    public void shouldNotMatchNoQuotes() { //arguably could be ambiguous
        LiteralMatcher matcher = new LiteralMatcher();

        matcher.check(new MyTokenizer("monkey"), checker, df);
    }
}