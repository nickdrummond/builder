package com.nickd.parser;

import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

import static com.nickd.parser.ParseTree.branch;
import static org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax.*;

public class MOSAxiomTreeParser {

    private final OWLDataFactory df;
    private final OWLEntityChecker checker;

    public MOSAxiomTreeParser(OWLDataFactory df,
                              OWLEntityChecker checker) {
        this.df = df;
        this.checker = checker;
    }

    public OWLAxiom parse(String s) {
        ParseTree builder = new ParseTree(s, checker, df)
                .expectEither(
                        propertyCharacteristicAxiom(),
                        declarationAxiom(),
                        individualAxiom(),
                        negativeIndividualAxiom(),
                        classAxiom(),
                        annotationPropertyAxiom(),
                        objectPropertyAxiom(),
                        dataPropertyAxiom()
                );
        return builder.getAxiom();
    }

    private ParseTree propertyCharacteristicAxiom() {
        return branch() // Property Characteristics
                .expectEither(
                        branch().expectPrefixKeyword(FUNCTIONAL).expectEither(
                                branch()
                                        .expectObjectPropertyExpression("p")
                                        .create(e -> df.getOWLFunctionalObjectPropertyAxiom(e.objPropExpr("p"))),
                                branch()
                                        .expectDataPropertyExpression("p")
                                        .create(e -> df.getOWLFunctionalDataPropertyAxiom(e.dataPropExpr("p")))
                        ),
                        branch()
                                .expectPrefixKeyword(TRANSITIVE).expectObjectPropertyExpression("p")
                                .create(e -> df.getOWLTransitiveObjectPropertyAxiom(e.objPropExpr("p"))),
                        branch()
                                .expectPrefixKeyword(SYMMETRIC).expectObjectPropertyExpression("p")
                                .create(e -> df.getOWLSymmetricObjectPropertyAxiom(e.objPropExpr("p"))),
                        branch()
                                .expectPrefixKeyword(ASYMMETRIC).expectObjectPropertyExpression("p")
                                .create(e -> df.getOWLAsymmetricObjectPropertyAxiom(e.objPropExpr("p"))),
                        branch()
                                .expectPrefixKeyword(REFLEXIVE).expectObjectPropertyExpression("p")
                                .create(e -> df.getOWLReflexiveObjectPropertyAxiom(e.objPropExpr("p"))),
                        branch()
                                .expectPrefixKeyword(IRREFLEXIVE).expectObjectPropertyExpression("p")
                                .create(e -> df.getOWLIrreflexiveObjectPropertyAxiom(e.objPropExpr("p")))
                );
    }

    private ParseTree declarationAxiom() {
        return branch() // Declaration axioms
                .expectEither(
                        branch().expectPrefixKeyword(CLASS).expectClass("c").create(e -> df.getOWLDeclarationAxiom(e.cls("c"))),
                        branch().expectPrefixKeyword(INDIVIDUAL).expectIndividual("i").create(e -> df.getOWLDeclarationAxiom(e.ind("i"))),
                        branch().expectPrefixKeyword(OBJECT_PROPERTY).expectObjectProperty("p").create(e -> df.getOWLDeclarationAxiom(e.objProp("p"))),
                        branch().expectPrefixKeyword(DATA_PROPERTY).expectDataProperty("p").create(e -> df.getOWLDeclarationAxiom(e.dataProp("p"))),
                        branch().expectPrefixKeyword(ANNOTATION_PROPERTY).expectAnnotationProperty("p").create(e -> df.getOWLDeclarationAxiom(e.annotProp("p")))
                );
    }

    private ParseTree individualAxiom() {
        return branch() // Individual axioms
                .expectIndividual("a")
                .expectEither(
                        branch()
                                .expectKeyword(TYPE).expectClassExpression("A")
                                .create(e -> df.getOWLClassAssertionAxiom(e.clsExpr("A"), e.ind("a"))),
                        branch()
                                .expectAnnotationProperty("p").expectLiteral("v")
                                .create(e -> df.getOWLAnnotationAssertionAxiom(e.annotProp("p"), e.ind("a").asOWLNamedIndividual().getIRI(), e.lit("v"))),
                        branch() // expected path
                                .expectObjectPropertyExpression("p").expectIndividual("b")
                                .create(e -> df.getOWLObjectPropertyAssertionAxiom(e.objPropExpr("p"), e.ind("a"), e.ind("b"))),
                        branch()
                                .expectDataPropertyExpression("p").expectLiteral("v")
                                .create(e -> df.getOWLDataPropertyAssertionAxiom(e.dataPropExpr("p"), e.ind("a"), e.lit("v"))),
                        branch()
                                .expectKeyword(SAME_AS).expectIndividual("o")
                                .create(e -> df.getOWLSameIndividualAxiom(e.ind("a"), e.ind("o")))
                );
    }

    private ParseTree negativeIndividualAxiom() {
        return branch() // Negative individual axioms
                .expectKeyword(NOT).expectKeyword(OPEN).expectIndividual("a")
                .expectEither(
                        branch() // expected path
                                .expectObjectPropertyExpression("p").expectIndividual("b").expectKeyword(CLOSE)
                                .create(e -> df.getOWLNegativeObjectPropertyAssertionAxiom(e.objPropExpr("p"), e.ind("a"), e.ind("b"))),
                        branch()
                                .expectDataPropertyExpression("p").expectLiteral("v").expectKeyword(CLOSE)
                                .create(e -> df.getOWLNegativeDataPropertyAssertionAxiom(e.dataPropExpr("p"), e.ind("a"), e.lit("v")))
                );
    }

    private ParseTree classAxiom() {
        return branch() // Class axioms
                .expectClass("c")
                .expectEither(
                        branch()
                                .expectKeyword(SUBCLASS_OF).expectClassExpression("s")
                                .create(e -> df.getOWLSubClassOfAxiom(e.cls("c"), e.clsExpr("s"))),
                        branch()
                                .expectKeyword(EQUIVALENT_TO).expectClassExpression("s")
                                .create(e -> df.getOWLEquivalentClassesAxiom(e.cls("c"), e.clsExpr("s"))),
                        branch()
                                .expectAnnotationProperty("p").expectLiteral("v")
                                .create(e -> df.getOWLAnnotationAssertionAxiom(e.annotProp("p"), e.cls("c").getIRI(), e.lit("v")))
                );
    }

    private ParseTree annotationPropertyAxiom() {
        return branch() // Annotation property axioms
                .expectAnnotationProperty("p")
                .expectEither(
                        branch()
                                .expectKeyword(RANGE).expectDatatype("d")
                                .create(e -> df.getOWLAnnotationPropertyRangeAxiom(e.annotProp("p"), e.datatype("d").getIRI())),
                        branch()
                                .expectAnnotationProperty("a").expectLiteral("v")
                                .create(e -> df.getOWLAnnotationAssertionAxiom(e.annotProp("a"), e.annotProp("p").getIRI(), e.lit("v")))
                );
    }

    private ParseTree dataPropertyAxiom() {
        return branch() // Data property axioms
                .expectDataProperty("p")
                .expectEither(
                        branch()
                                .expectAnnotationProperty("a").expectLiteral("v")
                                .create(e -> df.getOWLAnnotationAssertionAxiom(e.annotProp("a"), e.dataProp("p").getIRI(), e.lit("v"))),
                        branch()
                                .expectPrefixKeyword(RANGE).expectDatatype("d")
                                .create(e -> df.getOWLDataPropertyRangeAxiom(e.dataProp("p"), e.datatype("d")))
                );
    }

    private ParseTree objectPropertyAxiom() {
        return branch() // Object property axioms
                .expectObjectProperty("p")
                .expectEither(
                        branch()
                                .expectAnnotationProperty("a").expectLiteral("v")
                                .create(e -> df.getOWLAnnotationAssertionAxiom(e.annotProp("a"), e.objProp("p").getIRI(), e.lit("v"))),
                        branch()
                                .expectKeyword(INVERSE_OF).expectObjectProperty("i")
                                .create(e -> df.getOWLInverseObjectPropertiesAxiom(e.objProp("p"), e.objProp("i"))),
                        branch()
                                .expectKeyword(DOMAIN).expectClassExpression("c")
                                .create(e -> df.getOWLObjectPropertyDomainAxiom(e.objProp("p"), e.clsExpr("c"))),
                        branch()
                                .expectKeyword(RANGE).expectClassExpression("c")
                                .create(e -> df.getOWLObjectPropertyRangeAxiom(e.objProp("p"), e.clsExpr("c"))),
                        branch()
                                .expectPrefixKeyword(SUB_PROPERTY_OF).expectObjectPropertyExpression("s")
                                .create(e -> df.getOWLSubObjectPropertyOfAxiom(e.objProp("p"), e.objPropExpr("s"))),
                        branch()
                                .expectPrefixKeyword(EQUIVALENT_PROPERTIES).expectObjectPropertyExpression("p2")
                                .create(e -> df.getOWLEquivalentObjectPropertiesAxiom(e.objProp("p"), e.objPropExpr("p2")))//,
//                                        branch()
//                                                .expectList("ch", CHAIN_CONNECT, new ObjPropMatcher())
//                                                .expectKeyword(SUB_PROPERTY_CHAIN)
//                                                .expectObjectProperty("s")
//                                                .create(e -> df.getOWLSubPropertyChainOfAxiom(e.getList("ch"), e.objProp("s")))
                );
    }
}
