package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.DescriptionVisitorEx;
import com.nickd.util.Helper;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AcceptCommand implements Command {
    private Helper helper;

    public AcceptCommand(Helper helper) {
        this.helper = helper;
    }

    public Context handle(UserInput input, Context context) {
        OWLObject sel = null;
        if (!input.params().isEmpty()) { // this should be &1 and we get the axiom directly from the context
            String param = input.params().get(0);
            if (param.startsWith("&")) {
                sel = context.getSelectedObjects().get(Integer.parseInt(param.substring(1)));
            } else {
                // TODO could be any type of entity, axiom, whatever??
            }
        } else if (context.isSingleSelection()) {
            sel = context.getSelected();
        }

        if (sel != null) {
            List<OWLAxiom> axioms = ((OWLEntity) sel).accept(new DescriptionVisitorEx(context.getOntology(helper)));
            final OWLOntology target = (sel instanceof OWLNamedIndividual) ? helper.ont("star-wars.owl.ttl") : helper.ont("base.owl.ttl");
            helper.mngr.applyChanges(Stream.concat(
                    axioms.stream().map(a -> new AddAxiom(target, a)),
                    axioms.stream().map(a -> new RemoveAxiom(helper.suggestions, a))).collect(Collectors.toList()));
            System.out.println("Copied " + axioms.size() + "axioms to " + helper.render(target));

        }
        return context; // stay in current context
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Describes the context");
    }
}
