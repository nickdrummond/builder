package com.nickd.util;

import com.nickd.builder.Constants;
import org.semanticweb.owlapi.formats.RioTurtleDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.nickd.builder.Constants.BASE;

public class IOUtils {
    public static final int ONTOLOGY_FRESHNESS_POLL_MS = 5000;
    private final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    private final OWLOntologyManager mngr;
    private final Set<OWLOntology> changedOntologies = new HashSet<>();
    private final Set<OWLOntology> changedExternally = new HashSet<>();
    private final OWLOntology ont;
    private final long timestamp;

    public IOUtils(OWLOntology ont) {
        this.mngr = ont.getOWLOntologyManager();
        this.ont = ont;
        this.timestamp = System.currentTimeMillis();

        new Timer("check ontologies changed").schedule(new TimerTask() {
            @Override
            public void run() {
                checkIfChanged(ont.getImportsClosure());
            }
        }, ONTOLOGY_FRESHNESS_POLL_MS, ONTOLOGY_FRESHNESS_POLL_MS);

        mngr.addOntologyChangeListener(changes -> {
            // mark the ontologies as written to and check if files are fresh
            changes.stream().map(OWLOntologyChange::getOntology).distinct().forEach(changedOntologies::add);
            checkIfChanged(changedOntologies);
        });
    }

    public OWLOntology loadOrCreateSuggestions(OWLOntologyIRIMapper ontologyIRIMapper) throws OWLOntologyCreationException {
        IRI suggestionsLoc = ontologyIRIMapper.getDocumentIRI(Constants.SUGGESTIONS_BASE);
        try {
            final OWLOntology o = mngr.loadOntology(suggestionsLoc);
            logger.info("Loaded suggestions from: {}", suggestionsLoc);
            return o;
        } catch (OWLOntologyCreationException e) {
            logger.info("No suggestions at {}. Creating new suggestions ontology", suggestionsLoc);
            final OWLOntology o = mngr.createOntology(IRI.create(BASE + "/suggestions.owl.ttl"));
            ont.getOntologyID().getOntologyIRI().ifPresent(iri ->
                    o.applyChange(new AddImport(o, mngr.getOWLDataFactory().getOWLImportsDeclaration(iri))));
            mngr.setOntologyFormat(o, new TurtleDocumentFormat());
            return o;
        }
    }

    public void saveChanged() throws OWLOntologyStorageException {
        save(changedOntologies);
    }

    public void saveAll() throws OWLOntologyStorageException {
        save(mngr.getOntologies());
    }

    private void save(Set<OWLOntology> onts) throws OWLOntologyStorageException {
        for (OWLOntology o : onts) {
            setFormatAndPrefixes(o);
            mngr.saveOntology(o);
        }
    }

    private void setFormatAndPrefixes(OWLOntology o) {
        OWLDocumentFormat format = o.getOWLOntologyManager().getOntologyFormat(o);
        if (format instanceof RioTurtleDocumentFormat rioFormat) {
            TurtleDocumentFormat ttl = new TurtleDocumentFormat();
            ttl.copyPrefixesFrom(rioFormat);
            mngr.setOntologyFormat(o, ttl);
        }
    }

    private void checkIfChanged(Set<OWLOntology> onts) {
        for (OWLOntology o : onts) {
            try {
                File f = getSource(o).getCanonicalFile();
                long t = f.lastModified();
                if (t > this.timestamp) {
                    if (!changedExternally.contains(ont)) {
                        changedExternally.add(ont);
                        System.err.println("Ontology changed since loading: " + f);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private File getSource(OWLOntology o) {
        IRI docIRI = mngr.getOntologyDocumentIRI(o);
        File f = new File(docIRI.toURI());
        if (!f.exists()) {
            throw new RuntimeException(f + "does not exist for " + o.getOntologyID());
        }
        if (!f.isFile()) {
            throw new RuntimeException(f + "is not a file for " + o.getOntologyID());
        }

        return f;
    }
//
//    private void save(String location) throws OWLOntologyStorageException {
//        File base = new File(location);
//        System.out.println("Saving ontologies to " + base.getAbsolutePath());
//        if (!base.exists()) {
//            if (!base.mkdir()) {
//                throw new OWLOntologyStorageException("Could not create compilation directory: " + base);
//            }
//        }
//
//        for (OWLOntology o : mngr.getOntologies()) {
//            setFormatAndPrefixes(o);
//            try {
//                IRI iri = o.getOntologyID().getOntologyIRI().orElseThrow();
//                File f = new File(base, iri.getShortForm());
//                System.out.println("saving..." + f.getAbsolutePath());
//                FileOutputStream fileOutputStream = new FileOutputStream(f);
//                mngr.saveOntology(o, fileOutputStream);
//            } catch (FileNotFoundException e) {
//                throw new OWLOntologyStorageException(e);
//            }
//        }
//    }

}
