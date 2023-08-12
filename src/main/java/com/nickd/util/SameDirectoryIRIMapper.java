package com.nickd.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import javax.annotation.Nullable;
import java.io.File;

public class SameDirectoryIRIMapper implements OWLOntologyIRIMapper {

    private final File base;

    public SameDirectoryIRIMapper(File base) {
        if (base.isDirectory()) {
            this.base = base;
        }
        else {
            this.base = base.getParentFile();
        }
    }

    @Nullable
    @Override
    public IRI getDocumentIRI(IRI iri) {
        return IRI.create(new File(base, iri.getShortForm()));
    }
}
