package com.nickd.builder.command;

import com.nickd.builder.Context;
import com.nickd.builder.UserInput;

import java.util.List;

public interface Command {

    Context handle(UserInput commandStr, Context context);

    /**
     * Gets called if the current command ends with a string "?"
     * Could be used as a command help or to complete names in paramsAsString
     * @param commandStr
     * @param context
     * @return
     */
    List<String> autocomplete(UserInput commandStr, Context context);
}
