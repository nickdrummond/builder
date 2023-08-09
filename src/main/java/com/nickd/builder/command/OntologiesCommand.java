package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.*;

import java.util.List;

public class OntologiesCommand implements Command {
    private final Helper helper;

    public OntologiesCommand(Helper helper) {
        this.helper = helper;
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("List ontologies");
    }

    @Override
    public Context handle(UserInput input, Context parentContext) {
        List<String> params = input.params();
        if (params.size() == 1) {
            OWLOntology ont = helper.ont(params.get(0));
            if (ont != null) {
                return new OWLObjectListContext(helper.render(ont), parentContext, ont);
            }
        }
        return new OWLObjectListContext("ontologies", parentContext, List.copyOf(helper.mngr.getOntologies()));
    }
}
