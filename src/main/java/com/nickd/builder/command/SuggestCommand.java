package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.SuggestionContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SuggestCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(SuggestCommand.class);

    private final Helper helper;

    public SuggestCommand(Helper helper) {
        this.helper = helper;
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Do some voodoo with the given wookieepedia entry");
    }

    @Override
    public Context handle(UserInput input, Context context) {
        Stream<? extends OWLEntity> suggestions = null;
        if (!input.params().isEmpty()) {
            if (input.params().get(0).equalsIgnoreCase("C")) {
                suggestions = helper.suggestions.classesInSignature(Imports.EXCLUDED);
            }
            else if (input.params().get(0).equalsIgnoreCase("i")) {
                suggestions = helper.suggestions.individualsInSignature(Imports.EXCLUDED);
            }
        }

        if (suggestions == null) {
            suggestions = helper.suggestions.signature(Imports.EXCLUDED);
        }

        List<? extends OWLEntity> orderedSuggestions = suggestions
                .filter(e -> !helper.ont.containsEntityInSignature(e, Imports.INCLUDED))
                .sorted().toList();

        return new SuggestionContext(helper.suggestions,
                new OWLObjectListContext("suggestions", context, orderedSuggestions));
    }
}
