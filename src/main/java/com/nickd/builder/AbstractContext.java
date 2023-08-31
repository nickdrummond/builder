package com.nickd.builder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContext implements Context {

    @Nonnull
    protected final String name;

    protected final Context parent;

    public AbstractContext(@Nonnull String name, Context parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public Context getParent() {
        return parent;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public List<Context> stack(int promptDepth) {
        if (promptDepth == 0 || isRoot()) {
            return new ArrayList<>();
        }
        List<Context> stack = parent.stack(promptDepth-1);
        stack.add(this);
        return stack;
    }

    @Override
    public List<Context> stack() {
        if (isRoot()) {
            return new ArrayList<>();
        }
        List<Context> stack = parent.stack();
        stack.add(this);
        return stack;
    }
}
