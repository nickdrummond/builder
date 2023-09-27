package com.nickd.parser;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax.TYPE;

@RunWith(MockitoJUnitRunner.class)
public class MOSAxiomTreeParserTest {

    @Mock
    private OWLEntityChecker checker;

    private OWLDataFactoryImpl df;

    private MOSAxiomTreeParser parser;

    @Before
    public void createParser() {
        df = new OWLDataFactoryImpl();
        parser = new MOSAxiomTreeParser(df, checker);
    }

    @Test
    public void wellFormedPropertyCharacteristics() {
        assertEquals(df.getOWLTransitiveObjectPropertyAxiom(objP("op")), parser.parse("Transitive: op"));
        assertEquals(df.getOWLFunctionalObjectPropertyAxiom(objP("op")), parser.parse("Functional: op"));
        assertEquals(df.getOWLFunctionalDataPropertyAxiom(dataP("dp")), parser.parse("Functional: dp"));
    }

    @Test
    public void testClassAssertion() {
        OWLAxiom expected = df.getOWLClassAssertionAxiom(cls("A"), ind("a"));
        assertEquals(expected, parser.parse("a Type A"));
    }

    @Test
    public void testClassAssertionWithClassExpression() {
        OWLAxiom expected = df.getOWLClassAssertionAxiom(
                df.getOWLObjectSomeValuesFrom(objP("p"), cls("A")),
                ind("a"));
        assertEquals(expected, parser.parse("a Type (p some A)"));
    }

    @Test
    public void testObjectPropertyAssertion() {
        OWLAxiom expected = df.getOWLObjectPropertyAssertionAxiom(objP("p"), ind("a"), ind("b"));
        assertEquals(expected, parser.parse("a p b"));
    }

    @Test
    public void testDataPropertyAssertion() {
        OWLAxiom expected = df.getOWLDataPropertyAssertionAxiom(dataP("p"), ind("a"), lit("value"));
        assertEquals(expected, parser.parse("a p \"value\""));
    }

    @Test
    public void testNegativeObjectPropertyAssertion() {
        OWLAxiom expected = df.getOWLNegativeObjectPropertyAssertionAxiom(objP("p"), ind("a"), ind("b"));
        assertEquals(expected, parser.parse("not (a p b)"));
    }

    @Test
    public void testNegativeDataPropertyAssertion() {
        OWLAxiom expected = df.getOWLNegativeDataPropertyAssertionAxiom(dataP("p"), ind("a"), lit("value"));
        assertEquals(expected, parser.parse("not (a p \"value\")"));
    }

    @Test
    public void annotationAssertionAxiom() {
        OWLNamedIndividual a = ind("a");
        OWLAxiom expected = df.getOWLAnnotationAssertionAxiom(annotP("ap"), a.getIRI(), lit("value"));
        assertEquals(expected, parser.parse("a ap \"value\""));
    }

    @Test
    public void minimumPropertyChainAxiom() {
        List<OWLObjectPropertyExpression> chain = List.of(objP("prop1"), objP("prop2"));
        OWLAxiom expected = df.getOWLSubPropertyChainOfAxiom(chain, objP("sProp"));
        assertEquals(expected, parser.parse("prop1 o prop2 SubPropertyOf: sProp"));
    }

    @Test
    public void longerPropertyChainAxiom() {
        List<OWLObjectPropertyExpression> chain = List.of(
                objP("prop1"),
                objP("prop2"),
                objP("prop3"),
                objP("prop4"));
        OWLAxiom expected = df.getOWLSubPropertyChainOfAxiom(chain, objP("sProp"));
        assertEquals(expected, parser.parse("prop1 o prop2 o prop3 o prop4 SubPropertyOf: sProp"));
    }

    @Test
    public void disjointClassesAxiom_single() {
        List<OWLClass> classes = List.of(
                cls("c1"));
        OWLAxiom expected = df.getOWLDisjointClassesAxiom(classes);
        assertEquals(expected, parser.parse("DisjointClasses: c1"));
    }

    @Test
    public void disjointClassesAxiom_two() {
        List<OWLClass> classes = List.of(
                cls("c1"),
                cls("c2"));
        OWLAxiom expected = df.getOWLDisjointClassesAxiom(classes);
        assertEquals(expected, parser.parse("DisjointClasses: c1, c2"));
    }

    @Test
    public void disjointClassesAxiom_many() {
        List<OWLClass> classes = List.of(
                cls("c1"),
                cls("c2"),
                cls("c3"),
                cls("c4"));
        OWLAxiom expected = df.getOWLDisjointClassesAxiom(classes);
        assertEquals(expected, parser.parse("DisjointClasses: c1, c2, c3, c4"));
    }

    @Test
    public void missingPropertyOrKeywordInIndividualAxiom() {
        try {
            ind("anindividual");
            parser.parse("anindividual");
            fail("Expected exception not thrown");
        }
        catch (ParserException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            assertTrue(e.isObjectPropertyNameExpected());
            assertTrue(e.isDataPropertyNameExpected());
            assertTrue(e.getExpectedKeywords().contains(TYPE.toString()));
            assertEquals(12, e.getStartPos());
        }
    }

    @Test
    public void missingIndividualInPropertyAssertionAxiom() {
        try {
            ind("anindividual");
            objP("prop");
            parser.parse("anindividual prop droo");
            fail("Expected exception not thrown");
        }
        catch (ParserException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            assertTrue(e.isIndividualNameExpected());
            assertEquals(18, e.getStartPos());
        }
    }

    @Test
    public void missingClassInClassAssertionAxiom() {
        try {
            ind("anindividual");
            parser.parse("anindividual Type droo");
            fail("Expected exception not thrown");
        }
        catch (ParserException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            assertTrue(e.isClassNameExpected());
            assertEquals(18, e.getStartPos());
        }
    }

    @Test
    public void missingClosingBracket() {
        OWLNamedIndividual one = ind("one");
        OWLObjectProperty boo = objP("boo");
        OWLClass aClass = cls("AClass");

        try {
            parser.parse("one Type boo some (AClass");
        }
        catch (ParserException e) {
            System.out.println("e = " + e);
            assertThat(e.getExpectedKeywords(), CoreMatchers.hasItem(")"));
            assertEquals(26, e.getColumnNumber());
            assertEquals("|EOF|", e.getCurrentToken());
            assertEquals(6, e.getTokenSequence().size());
        }
    }

    private OWLClass cls(String name) {
        OWLClass cls = df.getOWLClass(name);
        when(checker.getOWLClass(name)).thenReturn(cls);
        return cls;
    }

    private OWLNamedIndividual ind(String name) {
        OWLNamedIndividual ind = df.getOWLNamedIndividual(name);
        when(checker.getOWLIndividual(name)).thenReturn(ind);
        return ind;
    }

    private OWLObjectProperty objP(String name) {
        OWLObjectProperty prop = df.getOWLObjectProperty(name);
        when(checker.getOWLObjectProperty(name)).thenReturn(prop);
        return prop;
    }

    private OWLDataProperty dataP(String name) {
        OWLDataProperty prop = df.getOWLDataProperty(name);
        when(checker.getOWLDataProperty(name)).thenReturn(prop);
        return prop;
    }

    private OWLAnnotationProperty annotP(String name) {
        OWLAnnotationProperty prop = df.getOWLAnnotationProperty(name);
        when(checker.getOWLAnnotationProperty(name)).thenReturn(prop);
        return prop;
    }

    private OWLLiteral lit(String value) {
        return df.getOWLLiteral(value);
    }
}