package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import org.semanticweb.owlapi.model.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IndividualsCommand implements Command {
    private App app;

    public IndividualsCommand(App app) {
        this.app = app;
    }

    @Override
    public Context handle(UserInput input, Context context) {
        OWLOntology ont = context.getOntology();
        Optional<OWLClass> cls = context.getOWLClass();
        List<OWLNamedIndividual> results = cls.isPresent() ? getInstances(cls.get()) : getAllIndividualsInSig(ont);
        return new OWLObjectListContext("individuals", context, results);
    }

    private List<OWLNamedIndividual> getInstances(OWLClass cls) {
        return app.told.instances(cls).sorted().collect(Collectors.toList());
    }

    private List<OWLNamedIndividual> getAllIndividualsInSig(OWLOntology ont) {
        return ont.individualsInSignature().sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Get instances of a class in the context or all individuals");
    }
}
