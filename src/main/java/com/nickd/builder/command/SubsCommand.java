package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.OWLObjectListContext;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SubsCommand implements Command {
    private App app;

    public SubsCommand(App app) {
        this.app = app;
    }

    @Override
    public Context handle(UserInput input, Context context) {
        Optional<OWLClass> cls = context.getOWLClass();
        List<OWLClass> results = cls.isPresent() ? getSubs(cls.get()) : getSubs(app.df.getOWLThing());
        return new OWLObjectListContext("subs", context, results);
    }

    private List<OWLClass> getSubs(OWLClass cls) {
        return app.told.subClasses(cls).sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Get instances of a class in the context or all individuals");
    }
}
