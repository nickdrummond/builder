package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.SuggestionContext;
import com.nickd.builder.UserInput;
import com.nickd.util.*;
import com.nickd.wiki.WikiPage;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.nickd.wiki.Wiki.forString;

/**
 * suggest https://starwars.fandom.com/wiki/Bix_Caleen
 * suggest Bix_Caleen (existing entity)
 * 1) Cassian_Andor
 * 2)
 */

public class WikiCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(WikiCommand.class);

    private final Helper helper;
    private final OWLAnnotationProperty defaultSearchLabel;

    public WikiCommand(Helper helper, OWLAnnotationProperty defaultSearchLabel) {
        this.helper = helper;
        this.defaultSearchLabel = defaultSearchLabel;
    }


    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Do some voodoo with the given wookieepedia entry");
    }

    @Override
    public Context handle(UserInput input, Context context) {

        if (input.fullText().contains("&")) {
            input = replaceVars(input, context);
        }
        // TODO validate(input);
        List<String> params = input.params();

        if (params.size() >= 1) {
            String refUrl = params.get(0);

            try {
                WikiPage wikiPage = forString(refUrl, helper);

                System.out.println(wikiPage.getIri());

                if (params.size() == 2) {
                    String query = params.get(1);
                    if (query.equals("suggest")) {
                        return new SuggestionContext(helper.suggestions, new OWLObjectListContext(refUrl, context, wikiPage.getUnknown()));
                    }
                }
                return new OWLObjectListContext(refUrl, context, wikiPage.getKnown());
            } catch (IOException e) {
                logger.warn("Cannot find Wookieepedia for ${}", refUrl);
            }
        }
        return context;
    }

    private UserInput replaceVars(UserInput input, Context currentContext) {
        List<String> names = currentContext.getSelectedObjects().stream().map(helper::render).collect(Collectors.toList());
        return new UserInput(MyStringUtils.replaceVars(input.fullText(), names));
    }
}
