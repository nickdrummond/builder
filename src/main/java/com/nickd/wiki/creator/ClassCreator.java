package com.nickd.wiki.creator;

import com.nickd.builder.Constants;
import com.nickd.util.FinderUtils;
import com.nickd.util.App;
import com.nickd.wiki.Wiki;
import com.nickd.wiki.WikiPage;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ClassCreator implements Creator<OWLClass> {
    private final String selector;
    private String parentSelector;
    private String subclassesSelector;
    private String typeName;

    public ClassCreator(String selector) {
        this.selector = selector;
    }

    @Override
    public ClassCreator withType(String typeName) {
        this.typeName = typeName;
        return this;
    }

    @Override
    public ClassCreator withRelation(String relationName, String subjectSelector) {
        throw new NotImplementedException("TODO");
    }

    public ClassCreator withCommonParent(String parentSelector) {
        this.parentSelector = parentSelector;
        return this;
    }

    public ClassCreator withSubclasses(String subclassesSelector) {
        this.subclassesSelector = subclassesSelector;
        return this;
    }

    @Override
    public void build(WikiPage wikiPage) {
        getSubjects(wikiPage).forEach(href -> indexEntity(href, wikiPage));
    }

    private Stream<IRI> getSubjects(WikiPage wikiPage) {

        Elements parent = wikiPage.getDocument().select("body");

        if (parentSelector != null) {
            parent = parent.select(parentSelector);
        }

        if (subclassesSelector != null) {
            makeSubclasses(wikiPage, parent);
        }

        return getLinks(parent);
    }

    private void makeSubclasses(WikiPage wikiPage, Elements parents) {
        App app = wikiPage.getHelper();

        List<OWLOntologyChange> changes = new ArrayList<>();
        parents.forEach(parent -> {
            Elements superclassNode = parent.select(selector);
            if (!superclassNode.isEmpty()) {
                String superclassHref = superclassNode.get(0).attr("href");
                OWLClass superclass = clsForHref(superclassHref, wikiPage);

                Elements subclasses = parent.select(subclassesSelector);
                subclasses.forEach(sub -> {
                    String subclassHref = sub.attr("href");
                    OWLClass subclass = clsForHref(subclassHref, wikiPage);
                    changes.add(new AddAxiom(app.suggestions, app.df.getOWLSubClassOfAxiom(subclass, superclass)));
                });
            }
        });
        app.mngr.applyChanges(changes);
    }

    private Stream<IRI> getLinks(Elements parent) {
        Elements selected = parent.select(selector);
        return getHrefs(selected).map(h -> IRI.create(Wiki.WIKI + h));
    }

    private static Stream<String> getHrefs(Elements selected) {
        return selected.stream()
                .map(l -> l.attr("href"))
                .distinct()
                .filter(h -> h.startsWith(Wiki.PATH));
    }

    private void indexEntity(IRI iri, WikiPage page) {
        App app = page.getHelper();

        String iriString = iri.getIRIString();
        List<OWLEntity> matches = FinderUtils.annotationExact(iriString, app.df.getRDFSSeeAlso(), app);
        if (matches.isEmpty()) {
            OWLEntity entity = create(Wiki.pageName(iri), iri, app);
            page.addSuggestion(entity, iriString);
        } else {
            matches.forEach(e -> page.addKnownEntities(e, iriString));
        }
    }

    @Override
    public OWLClass create(String name, IRI iri, App app) {
        OWLAnnotationProperty editorLabel = app.annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        OWLClass rootType = app.cls(typeName);
        EntityBuilder entityBuilder = new EntityBuilder(app, editorLabel);
        return entityBuilder.buildCls(rootType, name, iri, app.suggestions);
    }

    private OWLClass clsForHref(String href, WikiPage wikiPage) {
        String name = href.substring(href.lastIndexOf("/") + 1);
        return wikiPage.getHelper().cls(name); // TODO lookup if entity already exists with this ref
    }
}
