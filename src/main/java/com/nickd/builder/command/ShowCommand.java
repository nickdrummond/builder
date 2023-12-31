package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import com.nickd.util.DescriptionVisitorEx;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;

public class ShowCommand implements Command {
    private App app;

    public ShowCommand(App app) {
        this.app = app;
    }

    public Context handle(UserInput input, Context context) {
        OWLObject sel = null;
        if (!input.params().isEmpty()) { // this should be &1 and we get the axiom directly from the context
            String param = input.params().get(0);
            if (param.startsWith("&")) {
                sel = context.getSelectedObjects().get(Integer.parseInt(param.substring(1)));
            }
            else {
                // TODO could be any type of entity, axiom, whatever??
            }
        }
        else if (context.isSingleSelection()) {
            sel = context.getSelected();
        }

        if (sel != null) {
            if (sel instanceof OWLEntity) {
                List<OWLAxiom> axioms = ((OWLEntity)sel).accept(new DescriptionVisitorEx(context.getOntology()));
                return new OWLObjectListContext("axioms", context, axioms);
            }
            else {
                return new OWLObjectListContext("entities", context, new ArrayList<>(sel.getSignature()));
            }
        }
        return context;
    }
//
//    @Override
//    public Context handle(UserInput input, Context context) {
//        Optional<? extends OWLObject> sel = Optional.empty();
//
//        if (input.params().size() == 1) {
//            sel = app.entity(input.paramsAsString());
//        } else if (context.isSingleSelection()) {
//            sel = Optional.of(context.getSelected());
//        }
//
//        return sel.map(e -> createDescriptionContext(context, e)).orElse(context);
//    }
//
//    private Context createDescriptionContext(Context context, OWLObject e) {
//        return new OWLObjectListContext("axioms", context, e.accept(new DescriptionVisitorEx(context.getOntology(app))));
//    }


    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Describes the context");
    }
}
