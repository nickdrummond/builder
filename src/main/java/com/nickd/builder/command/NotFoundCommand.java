package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotFoundCommand implements Command {

    private Logger logger = LoggerFactory.getLogger(NotFoundCommand.class);

    private final Map<String, Command> commands;

    public NotFoundCommand(Map<String, Command> commands) {
        this.commands = commands;
    }

    @Override
    public Context handle(UserInput input, Context context) {
        logger.warn("Command not found: " + input);
        return context;
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return new ArrayList<>(commands.keySet());
    }
}
