package com.nickd.parser;

import com.nickd.util.Helper;
import org.junit.Test;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

        List<OWLAxiom> failed = new ArrayList<>();

        helper.ont.axioms(Imports.INCLUDED).forEach( ax -> {
            Set<OWLAnnotation> annotations = ax.getAnnotations();
            if (!annotations.isEmpty()) {
                System.out.println("stripping axiom annotations = " + annotations);
                ax = ax.getAxiomWithoutAnnotations();
                System.out.println("on axiom: " + ax);
            }

            // TODO lists
            if (ax.isOfType(
                    AxiomType.DISJOINT_CLASSES,
                    AxiomType.DISJOINT_UNION,
                    AxiomType.DIFFERENT_INDIVIDUALS,
                    AxiomType.DISJOINT_OBJECT_PROPERTIES,
                    AxiomType.DISJOINT_DATA_PROPERTIES
            )) {
                skipped.add(ax);
                return;
            }

            String axiomStr = helper.render(ax);

            try {
                helper.mosAxiom(axiomStr);
            }
            catch(ParserException e) {
                System.out.println(axiomStr);
                System.err.println(e.getMessage());
                failed.add(ax);
            }
        });

        assertEquals(0, failed.size());

        skipped.forEach( ax -> System.out.println("skipped axiom = " + helper.render(ax)) );
    }
}
