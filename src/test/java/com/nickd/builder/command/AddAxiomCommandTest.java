package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.RootContext;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AddAxiomCommandTest {

    private App app;
    private OWLAnnotationProperty rdfsLabel;
    private AddAxiomCommand command;
    private RootContext rootContext;

    @Before
    public void setup() throws OWLOntologyCreationException {
        app = new App();
        rdfsLabel = app.df.getRDFSLabel();
        command = new AddAxiomCommand(app, rdfsLabel);
        rootContext = new RootContext(app.ont);
    }

    @Test
    public void whatDoesTheExpressionParserDo() {
        prop("prop");
        cls("Chewbacca");
        String expression = "prop some Chew";

        try {
            app.mos(expression);
            fail();
        }
        catch(ParserException e) {
            // Start pos is the beginning of the token
            assertEquals(expression.indexOf("Chew"), e.getStartPos());
            // And column number is the same, starting 1
            assertEquals(expression.indexOf("Chew")+1, e.getColumnNumber());
        }
    }

    @Test
    public void whatDoesTheAxiomParserDo() {
        ind("Chewbacca");
        cls("Wookie");
        String axiom = "Chewbacca Type Wook";

        try {
            app.mosAxiom(axiom);
            fail();
        }
        catch(ParserException e) {
            // Start pos is the beginning of the token
            assertEquals(axiom.indexOf("Wook"), e.getStartPos());
            // And column number is the same, starting 1
            assertEquals(axiom.indexOf("Wook")+1, e.getColumnNumber());
        }
    }

    @Test
    public void whatDoesTheAxiomParserDoAgain() {
        ind("monkey");
        prop("hasThe");
        ind("twelve");

        String axiom = "monkey hasThe twe";
        try {
            app.mosAxiom(axiom);
            fail();
        } catch (ParserException e) {
            // Start pos is the beginning of the token
            assertEquals(axiom.indexOf("tw"), e.getStartPos());
            // And column number is the same, starting 1
            assertEquals(axiom.indexOf("tw") + 1, e.getColumnNumber());
        }
    }

    @Test
    public void whatDoesTheCombinationDo() {
        ind("Chewbacca");
        prop("hadRole");
        cls("Trader");
        String axiom = "Chewbacca Type (hadRole some Tra";

        try {
            app.mosAxiom(axiom);
        }
        catch(ParserException e) {
            // Start pos is the beginning of the token
            assertEquals(axiom.indexOf("Tra"), e.getStartPos());
            // And column number is the same, starting 1
            assertEquals(axiom.indexOf("Tra")+1, e.getColumnNumber());
        }
    }

    @Test
    public void createsAPromptPlaceholderContextOnParseError() {
        ind("one");
        prop("p");
        OWLNamedIndividual two = ind("two");
        OWLNamedIndividual twit = ind("twit");

        Context result = command.handle(new UserInput("+ one p tw"), rootContext);
        assertEquals("+ one p ?tw?", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(twit, two));
    }


    @Test
    public void createsAPromptForType() {
        ind("one");
        OWLClass aClass = cls("AClass");
        OWLClass aClassAgain = cls("AClassAgain");

        Context result = command.handle(new UserInput("+ one Type AC"), rootContext);
        assertEquals("+ one Type ?AC?", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(aClassAgain, aClass));
    }

    @Test
    public void createsAPromptForTypeWithBrackets() {
        ind("one");
        prop("boo");
        cls("AClass");

        Context result = command.handle(new UserInput("+ one Type boo some (AClass"), rootContext);
        assertEquals("+ one Type boo some (AClass ??", result.getName());
    }

    @Test
    public void createsAPromptInTheCorrectPlace() {
        OWLNamedIndividual i1 = ind("Bix_Contact");
        OWLNamedIndividual i2 = ind("Bix_Caleen");
        prop("participant");

        Context result = command.handle(new UserInput("+ Bix_Contact participant Bix"), rootContext);
        assertEquals("+ Bix_Contact participant ?Bix?", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(i1, i2));
    }

    @Test
    public void doesNotStackOverflowWhenReferenceInSuggest() throws OWLOntologyCreationException {
        ind("Chewbacca");

        // Alien only available in suggest
        OWLClass alien = app.cls("Alien");
        ent("Alien", alien, app.suggestions);

        Context result = command.handle(new UserInput("+ Chewbacca Type Alien"), rootContext);
        assertEquals(rootContext, result);
    }

    @Test
    public void doesNotStackOverflowWhen() {
        OWLNamedIndividual chewie = ind("Chewbacca");
        prop("hadRole");
        OWLNamedIndividual soloBff = ind("SoloBFF");

        Context result = command.handle(new UserInput("+ Chewbacca hadR"), rootContext);
        assertEquals("+ Chewbacca hadRole ??", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(chewie, soloBff));
    }


    @Test
    public void doesNotStackOverflowWhenNoHints() {
        OWLNamedIndividual chewie = ind("Chewbacca");
        prop("hadRole");
        OWLNamedIndividual soloBff = ind("SoloBFF");

        Context result = command.handle(new UserInput("+ Chewbacca hadRole"), rootContext);
        assertEquals("+ Chewbacca hadRole ??", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(chewie, soloBff));
    }

    private OWLObjectProperty prop(String name) {
        OWLObjectProperty p = app.prop(name);
        return ent(name, p);
    }

    private OWLNamedIndividual ind(String name) {
        OWLNamedIndividual ind = app.ind(name);
        return ent(name, ind);
    }

    private OWLClass cls(String name) {
        OWLClass ind = app.cls(name);
        return ent(name, ind);
    }

    private <T extends OWLEntity> T ent(String name, T ent) {
        return ent(name, ent, app.ont);
    }

    private <T extends OWLEntity> T ent(String name, T ent, OWLOntology ont) {
        ont.addAxiom(app.df.getOWLDeclarationAxiom(ent));
        ont.addAxiom(app.df.getOWLAnnotationAssertionAxiom(app.df.getRDFSLabel(), ent.getIRI(), app.df.getOWLLiteral(name)));
        return ent;
    }
}