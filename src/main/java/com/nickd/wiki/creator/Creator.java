package com.nickd.wiki.creator;

import com.nickd.util.App;
import com.nickd.wiki.WikiPage;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

public interface Creator<T extends OWLEntity> {

    T create(String name, IRI iri, App app);

    Creator<T> withType(String typeName);

    Creator<T> withCommonParent(String parentSelector);

    Creator<T> withRelation(String relationName, String subjectSelector);

    void build(WikiPage wikiPage);
}
