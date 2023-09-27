package com.nickd.parser.matcher;

import com.nickd.parser.MyTokenizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClassListMatcherTest {

    @Mock
    private OWLEntityChecker checker;

    private OWLDataFactoryImpl df;


    @Before
    public void createParser() {
        df = new OWLDataFactoryImpl();
    }

    @Test
    public void matches() {
        OWLClass a = cls("a");
        OWLClass b = cls("b");

        ClassListMatcher matcher = new ClassListMatcher(",");

        matcher.check(new MyTokenizer("a, b"), checker, df);

        assertEquals(List.of(a, b), matcher.getObjectList(OWLClassExpression.class));
    }

    private OWLClass cls(String name) {
        OWLClass cls = df.getOWLClass(name);
        when(checker.getOWLClass(name)).thenReturn(cls);
        return cls;
    }
}
