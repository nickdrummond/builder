package com.nickd.wiki.creator;

import com.nickd.util.Helper;
import com.nickd.wiki.WikiPage;
import org.jsoup.nodes.Document;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.List;
import java.util.stream.Stream;

public interface Creator<T extends OWLEntity> {

    T create(String name, IRI iri, Helper helper);

    Creator<T> withType(String typeName);

    Creator<T> withCommonParent(String parentSelector);

    Creator<T> withRelation(String relationName, String subjectSelector);

    void build(WikiPage wikiPage);
}
