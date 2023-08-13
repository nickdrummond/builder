package com.nickd.wiki.creator;

import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

public interface Creator<T extends OWLEntity> {

    T create(String name, IRI iri, Helper helper);
}
