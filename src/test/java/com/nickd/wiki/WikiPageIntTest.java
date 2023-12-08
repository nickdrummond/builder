package com.nickd.wiki;

import com.nickd.util.App;
import com.nickd.wiki.creator.Creator;
import com.nickd.wiki.creator.IndividualCreator;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class WikiPageIntTest {

    @Test
    public void selectsEntitiesFromPage() throws OWLOntologyCreationException, IOException {
        InputStream html = getClass().getClassLoader().getResourceAsStream("Clone_Cadets.html");

        App app = new App(); // empty ont

        List<Creator<? extends OWLEntity>> sel = List.of(
                new IndividualCreator(
                        "#app_characters + table + .appearances a, " +
                        "#app_canon_characters + table + .appearances a")
        );

        WikiPage page = new WikiPage(app, html, sel);

        List<OWLEntity> unknownEntities = page.getUnknown();

        unknownEntities.forEach(System.out::println);

        assertThat(unknownEntities, CoreMatchers.hasItem(app.ind("Echo")));
    }
}