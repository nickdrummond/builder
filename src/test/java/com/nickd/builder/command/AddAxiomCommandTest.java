package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.RootContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JUnit4.class)
public class AddAxiomCommandTest extends TestCase {

    private Helper helper;

    @Before
    public void setup() throws OWLOntologyCreationException {
        helper = new Helper();
    }

    @Test
    public void createsAPromptPlaceholderContext() {
        ind("one");
        prop("p");
        OWLNamedIndividual two = ind("two");
        OWLNamedIndividual twit = ind("twit");

        OWLAnnotationProperty label = helper.df.getRDFSLabel();
        AddAxiomCommand command = new AddAxiomCommand(helper, label);

        Context result = command.handle(new UserInput("+ one p tw"), new RootContext(helper.ont));
        assertEquals("+ one p ?tw?", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(twit, two));
    }


    @Test
    public void createsAPromptForType() {
        ind("one");
        OWLClass aClass = cls("AClass");
        OWLClass aClassAgain = cls("AClassAgain");

        OWLAnnotationProperty label = helper.df.getRDFSLabel();
        AddAxiomCommand command = new AddAxiomCommand(helper, label);

        Context result = command.handle(new UserInput("+ one Type AC"), new RootContext(helper.ont));
        assertEquals("+ one Type ?AC?", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(aClassAgain, aClass));
    }

    @Test
    public void createsAPromptForTypeWithBrackets() {
        ind("one");
        OWLObjectProperty boo = prop("boo");
        OWLObjectProperty property = prop("property");
        OWLObjectProperty property2 = prop("property2");
        OWLClass aClass = cls("AClass");
        OWLClass aClassAgain = cls("AClassAgain");

        OWLAnnotationProperty label = helper.df.getRDFSLabel();
        AddAxiomCommand command = new AddAxiomCommand(helper, label);

        Context result = command.handle(new UserInput("+ one Type boo some (AClass"), new RootContext(helper.ont));
        assertEquals("+ one Type boo some (AClass", result.getName());
        assertThat(new ArrayList<>(result.getSelectedObjects()), hasItems(property, property2));
    }

    private OWLObjectProperty prop(String name) {
        OWLObjectProperty p = helper.prop(name);
        return ent(name, p);
    }

    private OWLNamedIndividual ind(String name) {
        OWLNamedIndividual ind = helper.ind(name);
        return ent(name, ind);
    }


    private OWLClass cls(String name) {
        OWLClass ind = helper.cls(name);
        return ent(name, ind);
    }

    private <T extends OWLEntity> T ent(String name, T ind) {
        helper.ont.addAxiom(helper.df.getOWLDeclarationAxiom(ind));
        helper.ont.addAxiom(helper.df.getOWLAnnotationAssertionAxiom(helper.df.getRDFSLabel(), ind.getIRI(), helper.df.getOWLLiteral(name)));
        return ind;
    }
}