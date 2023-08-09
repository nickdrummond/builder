package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.FinderUtils;
import com.nickd.util.Helper;
import com.nickd.util.Wookieepedia;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.List;

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
    public Context handle(UserInput input, Context parentContext) {
        // TODO validate(input);
        List<String> params = input.params();

        if (params.size() >= 1) {
            String refUrl = params.get(0);

            try {
                Wookieepedia wookieepedia = getWookieepedia(refUrl);

                System.out.println(wookieepedia.getUri());

                //TODO unknown need to stay with their seeAlso for creation?

                if (params.size() == 2) {
                    String query = params.get(1);
                    if (query.equals("suggest")) {
                        return new OWLObjectListContext(refUrl, parentContext, wookieepedia.getUnknown());
                    }
                }
                return new OWLObjectListContext(refUrl, parentContext, wookieepedia.getKnown());
            } catch (IOException e) {
                logger.warn("Cannot find Wookieepedia for ${}", refUrl);
            }
        }
        return parentContext;
    }

    private Wookieepedia getWookieepedia(@Nonnull String ref) throws IOException {
            if (ref.startsWith("http")) {
                return Wookieepedia.forURI(helper, URI.create(ref));
            } else {
                List<OWLEntity> entities = FinderUtils.annotationExact(ref, defaultSearchLabel, helper);
                if (entities.isEmpty()) {
                    return Wookieepedia.forName(helper, ref);
                }
                else {
                    OWLEntity entity = entities.get(0);
                    return Wookieepedia.forOWLEntity(helper, entity);
                }
            }
    }
}
