package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.App;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SaveCommand implements Command {

    private Logger logger = LoggerFactory.getLogger(SaveCommand.class);

    private App app;

    public SaveCommand(App app) {
        this.app = app;
    }


    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return List.of("Save all changed ontologies");
    }

    @Override
    public Context handle(UserInput input, Context context) {
        try {
            if (input.params().size() == 1 && input.params().get(0).equals("all")) {
                app.io.saveAll();
            }
            else {
                app.io.saveChanged();
            }
        } catch (OWLOntologyStorageException e) {
            logger.error("Failed to save changed ontologies", e);
        }
        return context;
    }
}
