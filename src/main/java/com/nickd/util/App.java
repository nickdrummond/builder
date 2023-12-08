package com.nickd.util;

import com.nickd.builder.Constants;
import com.nickd.parser.MOSAxiomTreeParser;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxClassExpressionParser;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static com.nickd.builder.Constants.BASE;

public class App {

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public final IOUtils io;

    public final OWLAnnotationProperty defaultSearchLabel;

    public final OWLOntologyManager mngr;
    public final  OWLOntology ont;
    public final  OWLDataFactory df;

    public OWLReasoner r;

    public OWLReasoner told;

    public final ShortFormProvider sfp;
    public final BidirectionalShortFormProviderAdapter nameCache;
    private final ShortFormEntityChecker checker;
    private final ManchesterOWLSyntaxClassExpressionParser mos;
    private final MOSAxiomTreeParser mosAxiom;

    public final OWLOntology suggestions;

    public App() throws OWLOntologyCreationException {
        this(OWLOntologyManager::createOntology, ontologyIRI -> ontologyIRI);
    }

    public App(final File ontFile) throws OWLOntologyCreationException {
        this(ontFile, new SameDirectoryIRIMapper(ontFile));
    }

    public App(final File ontFile, final OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        this(mngr -> mngr.loadOntologyFromOntologyDocument(ontFile), ontologyIRIMapper);
    }

    public App(final String iri, final OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        this(mngr -> mngr.loadOntology(IRI.create(iri)), ontologyIRIMapper);
    }

    public App(LoadsOntology loadsOntology, OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        mngr = new OWLManager().get();
        mngr.setIRIMappers(Collections.singleton(ontologyIRIMapper));
        df = mngr.getOWLDataFactory();

        long start = System.currentTimeMillis();
        ont = loadsOntology.apply(mngr);

        long timeToLoad = System.currentTimeMillis() - start;
        logger.info("Loaded in {} ms", timeToLoad);

        io = new IOUtils(ont);

        suggestions = io.loadOrCreateSuggestions(ontologyIRIMapper);

        defaultSearchLabel = annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        sfp = new AnnotationValueShortFormProvider(List.of(defaultSearchLabel), Collections.emptyMap(), mngr);
        nameCache = new BidirectionalShortFormProviderAdapter(sfp);
        suggestions.getSignature(Imports.INCLUDED).forEach(nameCache::add);

        checker = new ShortFormEntityChecker(nameCache);
        mos = new ManchesterOWLSyntaxClassExpressionParser(df, checker);
        mosAxiom = new MOSAxiomTreeParser(df, checker);

        told = new StructuralReasonerFactory().createNonBufferingReasoner(ont, new SimpleConfiguration());

        mngr.addOntologyChangeListener(changes ->
            changes.stream().map(OWLOntologyChange::getOntology).distinct().forEach( o -> {
                o.signature().forEach(nameCache::add); // update the name caches
                // TODO should really also deal with deletes
            })
            // TODO kill reasoners
        );
    }

    @FunctionalInterface
    public interface LoadsOntology {
        OWLOntology apply(OWLOntologyManager mngr) throws OWLOntologyCreationException;
    }

    private static IRI makeIRI(String s) {
        if (!s.contains("%")) { // if not already encoded
            s = URLEncoder.encode(s, StandardCharsets.UTF_8);
        }
        return IRI.create(BASE + "#" + s);
    }

    public OWLNamedIndividual ind(String s) {
        return df.getOWLNamedIndividual(makeIRI(s));
    }

    public OWLObjectProperty prop(String s) {
        return df.getOWLObjectProperty(makeIRI(s));
    }

    public OWLDataProperty dataProp(String s) {
        return df.getOWLDataProperty(makeIRI(s));
    }

    public OWLClass cls(String s) {
        return df.getOWLClass(makeIRI(s));
    }

    public OWLAnnotationProperty annotProp(String s, String base) {return df.getOWLAnnotationProperty(IRI.create(base + "#" + s)); }

    public OWLAnnotationProperty annotProp(String s) {return annotProp(s, BASE); }

    public OWLLiteral lit(String value) { return df.getOWLLiteral(value); }

    public OWLLiteral lit(String value, String lang) { return df.getOWLLiteral(value, lang); }

    public OWLLiteral lit(String value, OWLDatatype datatype) { return df.getOWLLiteral(value, datatype); }

    public OWLOntology ont(String s) { return mngr.getOntology(IRI.create(BASE + "/" + s)); }

    public String render (OWLEntity entity) {
        return sfp.getShortForm(entity);
    }

    public Optional<OWLEntity> entity(String s) {
        return ont.entitiesInSignature(makeIRI(s)).findFirst();
    }

    public OWLClassExpression mos(String s) {
        return mos.parse(s);
    }

    public OWLAxiom mosAxiom(String s) throws ParserException {
        return mosAxiom.parse(s);
    }

    public String render(OWLObject o) {
        return render(o, true);
    }

    public String render(OWLObject o, boolean singleLine) {
        if (o instanceof OWLOntology ontology) {
            return ontology.getOntologyID().getDefaultDocumentIRI().map(IRI::getShortForm).orElse("anon");
        }
        else {
            StringWriter w = new StringWriter();
            o.accept(new MyMOSObjectRenderer(w, this));
            return singleLine ? MyStringUtils.singleLine(w.toString()) : w.toString();
        }
    }

    public Stream<OWLEntity> entitiesForIRI(IRI iri) {
        return ont.entitiesInSignature(iri, Imports.INCLUDED).distinct();
    }
}
