package com.nickd.wiki.creator;

import com.nickd.builder.Constants;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import com.nickd.wiki.Wiki;
import com.nickd.wiki.WikiPage;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ClassCreator implements Creator<OWLClass> {
    private String parentSelector;
    private final String selector;
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
        Elements selected = parent.select(selector);
        if (selected.isEmpty()) {
            System.err.println(selector + " matches nothing");
        }
        if (subclassesSelector != null) {
            makeSubclasses(wikiPage, parent);
        }
        return getLinks(selected);
    }

    private void makeSubclasses(WikiPage wikiPage, Elements parents) {
        Helper helper = wikiPage.getHelper();

        List<OWLOntologyChange> changes = new ArrayList<>();
        parents.forEach(parent -> {
            Elements superclassNode = parent.select(selector);
            if (superclassNode.isEmpty()) {
                System.out.println("selector.isEmpty() = " + selector);
            }
            else {
                String superclassHref = superclassNode.get(0).attr("href");
                OWLClass superclass = clsForHref(superclassHref, wikiPage);

                Elements subclasses = parent.select(subclassesSelector);
                subclasses.forEach(sub -> {
                    String subclassHref = sub.attr("href");
                    OWLClass subclass = clsForHref(subclassHref, wikiPage);
                    changes.add(new AddAxiom(helper.suggestions, helper.df.getOWLSubClassOfAxiom(subclass, superclass)));
                });
            }
        });
        helper.mngr.applyChanges(changes);
    }

    private OWLClass clsForHref(String href, WikiPage wikiPage) {
        String name = href.substring(href.lastIndexOf("/") + 1);
        return wikiPage.getHelper().cls(name); // TODO lookup if entity already exists with this ref
    }

    private Stream<IRI> getLinks(Elements selected) {
        return getHrefs(selected).map(h -> IRI.create(Wiki.WIKI + h));
    }

    private static Stream<String> getHrefs(Elements selected) {
        return selected.stream()
                .map(l -> l.attr("href"))
                .distinct()
                .filter(h -> h.startsWith(Wiki.PATH));
    }

    private void indexEntity(IRI iri, WikiPage page) {
        Helper helper = page.getHelper();

        String iriString = iri.getIRIString();
        List<OWLEntity> matches = FinderUtils.annotationExact(iriString, helper.df.getRDFSSeeAlso(), helper);
        if (matches.isEmpty()) {
            OWLEntity entity = create(page.wikiPageName(iri), iri, helper);
            page.addSuggestion(entity, iriString);
        } else {
            matches.forEach(e -> page.addKnownEntities(e, iriString));
        }
    }

    @Override
    public OWLClass create(String name, IRI iri, Helper helper) {
        OWLAnnotationProperty editorLabel = helper.annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        OWLClass rootType = helper.cls(typeName);
        EntityBuilder entityBuilder = new EntityBuilder(helper, editorLabel);
        return entityBuilder.buildCls(rootType, name, iri, helper.suggestions);
    }

}
