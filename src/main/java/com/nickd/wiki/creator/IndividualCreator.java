package com.nickd.wiki.creator;

import com.nickd.builder.Constants;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import com.nickd.wiki.Wiki;
import com.nickd.wiki.WikiPage;
import org.jsoup.select.Elements;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IndividualCreator implements Creator<OWLNamedIndividual> {

    private Logger logger = LoggerFactory.getLogger(IndividualCreator.class);

    private String parentSelector;
    private final String selector;
    private String subjectSelector;
    private String typeName;
    private String relationName;

    public IndividualCreator(String selector) {
        this.selector = selector;
    }

    @Override
    public IndividualCreator withType(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public IndividualCreator withCommonParent(String parentSelector) {
        this.parentSelector = parentSelector;
        return this;
    }

    @Override
    public IndividualCreator withRelation(String relationName, String subjectSelector) {
        this.relationName = relationName;
        this.subjectSelector = subjectSelector;
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
        if (relationName != null && subjectSelector != null) {
            makeRelations(wikiPage, parent);
        }
        return getLinks(selected);
    }

    private void makeRelations(WikiPage wikiPage, Elements parents) {
        Helper helper = wikiPage.getHelper();
        OWLObjectProperty rel = helper.prop(relationName);

        List<OWLOntologyChange> changes = new ArrayList<>();
        parents.forEach(parent -> {
            Elements object = parent.select(selector);
            if (object.isEmpty()) {
                logger.error("Empty " + selector + " from " + parent);
            }
            else {
                String objectStr = object.get(0).attr("href");
                OWLNamedIndividual objectInd = indForHref(objectStr, wikiPage);

                Elements subjects = parent.select(subjectSelector);
                subjects.forEach(sub -> {
                    String subjectStr = sub.attr("href");
                    OWLNamedIndividual subjectInd = indForHref(subjectStr, wikiPage);
                    changes.add(new AddAxiom(helper.suggestions, helper.df.getOWLObjectPropertyAssertionAxiom(rel, subjectInd, objectInd)));
                });
            }
        });
        helper.mngr.applyChanges(changes);
    }

    private OWLNamedIndividual indForHref(String href, WikiPage wikiPage) {
        String name = href.substring(href.lastIndexOf("/") + 1);
        return wikiPage.getHelper().ind(name); // TODO lookup if entity already exists with this ref
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
    public OWLNamedIndividual create(String name, IRI iri, Helper helper) {
        OWLAnnotationProperty editorLabel = helper.annotProp(Constants.EDITOR_LABEL, Constants.UTIL_BASE);
        OWLClass rootType = helper.cls(typeName);
        EntityBuilder entityBuilder = new EntityBuilder(helper, editorLabel);
        return entityBuilder.build(rootType, name, iri, helper.suggestions);
    }

}
