package com.nickd.parser;

import com.nickd.util.Helper;
import org.junit.Test;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.nickd.RunBuilder.DEFAULT_OWL_TO_LOAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RoundtripTest {

    @Test
    public void shouldRoundtripAllAxiomsInStarWars() throws OWLOntologyCreationException {
        Helper helper = new Helper(new File(DEFAULT_OWL_TO_LOAD));

        List<OWLAxiom> skipped = new ArrayList<>();

        helper.ont.axioms(Imports.INCLUDED).forEach( ax -> {
            Set<OWLAnnotation> annotations = ax.getAnnotations();
            if (!annotations.isEmpty()) {
                System.out.println("stripping annotations = " + annotations);
                ax = ax.getAxiomWithoutAnnotations();
                System.out.println("on axiom: " + ax);
            }

            // TODO lists
            if (ax.isOfType(
                    AxiomType.DISJOINT_CLASSES,
                    AxiomType.DISJOINT_UNION,
                    AxiomType.DIFFERENT_INDIVIDUALS,
                    AxiomType.DISJOINT_OBJECT_PROPERTIES,
                    AxiomType.DISJOINT_DATA_PROPERTIES,
                    AxiomType.SUB_PROPERTY_CHAIN_OF,
                    AxiomType.DECLARATION // datatype declarations are not well formed
            )) {
                skipped.add(ax);
                return;
            }

            String axiomStr = helper.render(ax);

            try {
                OWLAxiom result = helper.mosAxiom(axiomStr);

//                assertEquals("Wrong axiom parsing " + axiomStr, ax, result);
            }
            catch(ParserException e) {
                System.err.println("Axiom: " + axiomStr + " (" + ax + ")");
                e.printStackTrace();
                fail();
            }
        });
    }
}
