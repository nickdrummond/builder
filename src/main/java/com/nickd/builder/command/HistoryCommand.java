package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class HistoryCommand implements Command {

    private final Stack<UserInput> history;

    public HistoryCommand(Stack<UserInput> history) {
        this.history = history;
    }

    @Override
    public Context handle(UserInput commandStr, Context context) {
        return context;
    }

    @Override
    public List<String> autocomplete(UserInput commandStr, Context context) {
        return history.stream().map(UserInput::fullText).collect(Collectors.toList());
    }
}
