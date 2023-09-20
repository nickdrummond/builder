package com.nickd.util;

import com.nickd.builder.Constants;
import com.nickd.parser.MOSAxiomTreeParser;
import openllet.owlapi.OWLHelper;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.formats.RioTurtleDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static com.nickd.builder.Constants.BASE;

public class Helper {

    private final Logger logger = LoggerFactory.getLogger(Helper.class);

    public OWLAnnotationProperty getLabelAnnotationProp() {
        return defaultSearchLabel;
    }

    @FunctionalInterface
    public interface LoadsOntology {
        OWLOntology apply(OWLOntologyManager mngr) throws OWLOntologyCreationException;
    }

    OWLAnnotationProperty defaultSearchLabel;

    public OWLOntologyManager mngr;
    public OWLOntology ont;
    public OWLDataFactory df;
    public OWLReasoner r;
    public OWLReasoner told;

    private final ShortFormProvider sfp;
    public final BidirectionalShortFormProviderAdapter nameCache;
    private final ShortFormEntityChecker checker;
    private final ManchesterOWLSyntaxClassExpressionParser mos;
    private final MOSAxiomTreeParser mosAxiom;

    public OWLOntology suggestions;

    public long timeToLoad;
    public long timeToClassify;

    private final Set<OWLOntology> changedOntologies = new HashSet<>();

    public Helper() throws OWLOntologyCreationException {
        this(OWLOntologyManager::createOntology, ontologyIRI -> ontologyIRI);
    }

    public Helper(final File ontFile) throws OWLOntologyCreationException {
        this(ontFile, new SameDirectoryIRIMapper(ontFile));
    }

    public Helper(final File ontFile, final OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        this(mngr -> mngr.loadOntologyFromOntologyDocument(ontFile), ontologyIRIMapper);
    }

    public Helper(final String iri, final OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        this(mngr -> mngr.loadOntology(IRI.create(iri)), ontologyIRIMapper);
    }

    public Helper(LoadsOntology loadsOntology, OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        mngr = new OWLManager().get();
        mngr.setIRIMappers(Collections.singleton(ontologyIRIMapper));

        long start = System.currentTimeMillis();
        ont = loadsOntology.apply(mngr);
        timeToLoad = System.currentTimeMillis() - start;

        suggestions = loadOrCreateSuggestions(ontologyIRIMapper);

        df = mngr.getOWLDataFactory();

        defaultSearchLabel = annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        // TODO should use the given annotation prop (EDITOR_LABEL)
        sfp = new AnnotationValueShortFormProvider(List.of(defaultSearchLabel), Collections.emptyMap(), mngr);
        nameCache = new BidirectionalShortFormProviderAdapter(sfp);
        suggestions.getSignature(Imports.INCLUDED).forEach(nameCache::add);
        checker = new ShortFormEntityChecker(nameCache);
        mos = new ManchesterOWLSyntaxClassExpressionParser(df, checker);
        mosAxiom = new MOSAxiomTreeParser(df, checker);

        told = new StructuralReasonerFactory().createNonBufferingReasoner(ont, new SimpleConfiguration());

        mngr.addOntologyChangeListener(changes ->
            changes.stream().map(OWLOntologyChange::getOntology).distinct().forEach( o -> {
                changedOntologies.add(o); // mark the ontologies as written to
                o.signature().forEach(nameCache::add); // update the name caches
                // TODO should really also deal with deletes
            })
            // TODO kill reasoners
        );

        logger.info("Loaded in " + timeToLoad + "ms");
    }

    private OWLOntology loadOrCreateSuggestions(OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        IRI suggestionsLoc = ontologyIRIMapper.getDocumentIRI(Constants.SUGGESTIONS_BASE);
        try {
            final OWLOntology o = mngr.loadOntology(suggestionsLoc);
            logger.info("Loaded suggestions from: {}", suggestionsLoc);
            return o;
        }
        catch (OWLOntologyCreationException e) {
            logger.info("No suggestions at {}. Creating new suggestions ontology", suggestionsLoc);
            final OWLOntology o = mngr.createOntology(IRI.create(BASE + "/suggestions.owl.ttl"));
            ont.getOntologyID().getOntologyIRI().ifPresent(iri ->
                    o.applyChange(new AddImport(o, df.getOWLImportsDeclaration(iri))));
            mngr.setOntologyFormat(o, new TurtleDocumentFormat());
            return o;
        }
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

    public OWLEntity check(String name) {
        return checker.getOWLObjectProperty(name);
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
            o.accept(new MyMOSObjectRenderer(w, sfp));
            return singleLine ? MyStringUtils.singleLine(w.toString()) : w.toString();
        }
    }

    public void clearReasoner() {
        r.dispose();
        r = null;
    }

    public void classify() {

        final OWLHelper h = OWLHelper.createLightHelper(OpenlletReasonerFactory.getInstance().createReasoner(ont));

//        long start = System.nanoTime();

        r = h.getReasoner();
        // analogue to Protege "Classify"
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        r.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        r.precomputeInferences(InferenceType.DATA_PROPERTY_HIERARCHY);
        r.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        r.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        r.precomputeInferences(InferenceType.SAME_INDIVIDUAL);

//        timeToClassify = System.nanoTime() - start;
//        System.out.println("Classified in " + TimeUnit.NANOSECONDS.toMillis(timeToClassify) + "ms");
    }

    public void saveChanged() throws OWLOntologyStorageException {
        save(changedOntologies);
    }

    public void saveAll() throws OWLOntologyStorageException {
        save(mngr.getOntologies());
    }

    public void save(Set<OWLOntology> onts) throws OWLOntologyStorageException {
        for (OWLOntology o : onts) {
            OWLDocumentFormat format = o.getOWLOntologyManager().getOntologyFormat(o);
            if (format instanceof RioTurtleDocumentFormat) {
                TurtleDocumentFormat ttl = new TurtleDocumentFormat();
                ttl.copyPrefixesFrom((RioTurtleDocumentFormat)format);
                o.getOWLOntologyManager().setOntologyFormat(o, ttl);
            }
            mngr.saveOntology(o);
        }
    }

    public void save(String location) throws OWLOntologyStorageException {
        File base = new File(location);
        System.out.println("Saving ontologies to " + base.getAbsolutePath());
        if (!base.exists()) {
            if (!base.mkdir()) {
                throw new OWLOntologyStorageException("Could not create compilation directory: " + base);
            }
        }

        for (OWLOntology o : mngr.getOntologies()) {
            OWLDocumentFormat format = o.getOWLOntologyManager().getOntologyFormat(o);
            if (format instanceof RioTurtleDocumentFormat) {
                TurtleDocumentFormat ttl = new TurtleDocumentFormat();
                ttl.copyPrefixesFrom((RioTurtleDocumentFormat)format);
                o.getOWLOntologyManager().setOntologyFormat(o, ttl);
            }
            try {
                IRI iri = o.getOntologyID().getOntologyIRI().orElseThrow();
                File f = new File(base, iri.getShortForm());
                System.out.println("saving..." + f.getAbsolutePath());
                FileOutputStream fileOutputStream = new FileOutputStream(f);
                mngr.saveOntology(o, fileOutputStream);
            } catch (FileNotFoundException e) {
                throw new OWLOntologyStorageException(e);
            }
        }
    }

    public Stream<OWLEntity> entitiesForIRI(IRI iri, Stream<OWLOntology> ontologies) {
        return ontologies.flatMap(ont -> ont.entitiesInSignature(iri)).distinct();
    }
}
