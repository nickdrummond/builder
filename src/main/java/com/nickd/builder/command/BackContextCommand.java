package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;
import com.nickd.util.App;

import java.util.List;
import java.util.stream.Collectors;

public class BackContextCommand implements Command {

    private final App app;

    public BackContextCommand(App app) {
        this.app = app;
    }

    @Override
    public Context handle(UserInput input, Context context) {
        if (input.params().isEmpty()) {
            Context parent = context.getParent();
            return parent != null ? parent : context;
        }
        else {
            String searchFor = input.paramsAsString();
            return context.stack().stream()
                    .filter(c -> c.getName().equals(searchFor))
                    .findFirst().orElse(context);
        }
    }

    @Override
    public List<String> autocomplete(UserInput input, Context context) {
        return context.stack().stream().map(Context::getName).collect(Collectors.toList());
    }
}
