package com.nickd.builder;

import com.nickd.builder.command.*;
import com.nickd.util.Helper;
import com.nickd.util.MyStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

/**
 *
 */
public class BuilderController {

    private final Logger logger = LoggerFactory.getLogger(BuilderController.class);


    // previous context contains a ?placeholder?
    public static final String placeHolderPattern = "\\?(.)*\\?";

    private final Map<String, Command> commands;
    private final NotFoundCommand defaultCommand;

    private final Helper helper;
    private final PrintStream outputStream;

    // should autocomplete live in Context?
    private List<String> autocomplete = new ArrayList<>();

    private final Stack<UserInput> history = new Stack<>();
    private InputStream inputStream;

    public BuilderController(File file, PrintStream outputStream) throws OWLOntologyCreationException {
        helper = new Helper(file);
        this.outputStream = outputStream;
        OWLAnnotationProperty defaultSearchLabel = helper.getLabelAnnotationProp();


        // STATE needed instead of context
        // valid commands will be dependent on the current state
        commands = new HashMap<>();
        commands.put("find", new FindCommand(helper, defaultSearchLabel));
        commands.put("wiki", new WikiCommand(helper, defaultSearchLabel));
        commands.put("suggest", new SuggestCommand(helper));
        commands.put("accept", new AcceptCommand(helper, defaultSearchLabel));
        commands.put("ont", new OntologiesCommand(helper));
        commands.put("ind", new IndividualsCommand(helper));
        commands.put("subs", new SubsCommand(helper));
        commands.put("new", new NewInstanceCommand(helper, defaultSearchLabel));
        commands.put("+", new AddAxiomCommand(helper, defaultSearchLabel));
        commands.put("-", new RemoveAxiomCommand(helper, defaultSearchLabel));
        commands.put("move", new MoveAxiomCommand(helper, defaultSearchLabel));
        commands.put("show", new ShowCommand(helper));
        commands.put("<", new BackContextCommand(helper));
        commands.put("<<", new RootContextCommand());
        commands.put("history", new HistoryCommand(history));
        commands.put("undo", new UndoCommand(helper));
        commands.put("save", new SaveCommand(helper));
        // TODO
        defaultCommand = new NotFoundCommand(commands);
    }

    public void run(InputStream inputStream) {

        this.inputStream = inputStream;

        Scanner in = new Scanner(inputStream);

        Context currentContext = new RootContext(helper.ont);

        boolean exit = false;
        while (!exit) {

            outputStream.print(buildPrompt(currentContext));

            UserInput input = new UserInput(in.nextLine());

//            logger.debug(input.toString());

            if (input.isEmpty()) {
                // ignore
            } else if (input.isQuit()) { // should be another
                exit = true;
            }
            else {
                currentContext = handleInput(input, currentContext);
            }
        }

        in.close();
    }

    private void outputOptions(List<String> results) {
        for (int i = 0; i < results.size(); i++) {
            outputStream.println(i + ") " + results.get(i));
        }
    }

    private Context handleInput(UserInput input, final Context currentContext) {

        try {
            // this flow is fxxxxx
            if (input.isIndex()) { // modify
                if (input.index() < autocomplete.size()) { // autocomplete selection
                    input = history.pop().autocomplete(autocomplete.get(input.index())); // rewrite history
                    autocomplete.clear();
                    // Should somehow write into the input stream
//                    outputStream.print(input.fullText());
//                    return currentContext;
                    // and should now allow other commands
                } else if (input.index() < currentContext.getSelectedObjects().size()) { // selecting an object
                    // this whole idea needs review
                    List<? extends OWLObject> selectedObjects = currentContext.getSelectedObjects();
                    OWLObject owlObject = selectedObjects.get(input.index());
                    Context c = replacePlaceholderInAncestorContext(owlObject, currentContext);
                    return (c != null) ? c : new OWLObjectListContext(helper.render(owlObject), currentContext, owlObject);
                }
            }

            history.push(input);

            if (input.isAutocompleteRequest()) { // autocomplete lookup
                Command command = getCommand(input);
                autocomplete = command.autocomplete(input, currentContext);
                outputOptions(autocomplete);
            } else {
                return performCommand(input, currentContext);
            }

        }
        catch(Exception e) { // don't drop out
            logger.error("error during command " + input, e);
        }
        return currentContext;
    }

    @Nonnull
    private Context performCommand(@Nonnull UserInput input, @Nonnull Context currentContext) {
        Command command = getCommand(input);
        Context c = command.handle(input, currentContext);
        if (c != currentContext) {
            c.renderSelection(outputStream, helper);
        }
        return c;
    }

    private Command getCommand(UserInput input) {
        return commands.getOrDefault(input.command(), defaultCommand);
    }

    private Context replacePlaceholderInAncestorContext(OWLObject owlObject, Context currentContext) {
        if (currentContext.isRoot()) {
            return null;
        }

        String name = currentContext.getName();
        String withReplacement = name.replaceFirst(placeHolderPattern, helper.render(owlObject));
        if (!withReplacement.equals(name)) { // we've substituted the placeholder
            return performCommand(new UserInput(withReplacement), currentContext.getParent());
        }

        return replacePlaceholderInAncestorContext(owlObject, currentContext.getParent());
    }

    private String buildPrompt(Context currentContext) {
        if (currentContext.isRoot()) {
            return Constants.PROMPT;
        }

        int depth = currentContext.stack().size();
        List<String> stackRen = currentContext
                .stack(Constants.PROMPT_DEPTH).stream()
                .map(c -> (c == currentContext) ? c.getName() : MyStringUtils.truncate(c.getName(), Constants.MAX_BEFORE_TRUNCATE, Constants.TRUNCATE_LENGTH))
                .toList();
        String breadcrumb = StringUtils.join(stackRen, Constants.BREADCRUMB) + Constants.PROMPT;
        if (stackRen.size() < depth) {
            breadcrumb = (depth-Constants.PROMPT_DEPTH) + "…" + Constants.BREADCRUMB + breadcrumb;
        }
        return breadcrumb;
    }
}
