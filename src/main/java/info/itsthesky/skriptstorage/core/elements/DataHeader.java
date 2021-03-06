package info.itsthesky.skriptstorage.core.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.Kleenean;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.FlatFile;
import info.itsthesky.skriptstorage.api.MultiplyPropertyExpression;
import info.itsthesky.skriptstorage.api.Utils;
import info.itsthesky.skriptstorage.api.queue.Queue;
import info.itsthesky.skriptstorage.api.queue.QueueManager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Name("YAML Headers")
@Description({"Represent the header at the top of an YAML file.",
        "This property can only work with YAML files, and will simply do nothing if used with JSON or TOML",
        "The comment # are already included, means you should not add them yourself!",
        "The framed mark will simply make the header text between a frame, looking more professional."})
@Examples({"set header of \"config\" to \"First line\" and \"Second line\"",
        "set framed header of \"config\" to \"First line\""})
public class DataHeader extends MultiplyPropertyExpression<String, String> {

    static {
        register(
                DataHeader.class,
                String.class,
                "[storage] [(data|yaml)] [file] [(1¦framed)] header[s]",
                "string"
        );
    }

    private Node node;
    private boolean framed;

    @Override
    public Class<?>[] acceptChange(Changer.@NotNull ChangeMode mode) {
        return new Class[] {String.class, String[].class};
    }

    @Override
    public void change(@NotNull Event e, Object[] delta, Changer.@NotNull ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        final String path = CreateShortcut.parse(getExpr().getSingle(e), node);
        if (path == null)
            return;
        final FlatFile flatFile = Utils.parse(path);
        final @Nullable Queue queue = QueueManager.parse(path);
        Consumer<FlatFile> consumer = null;
        if (!(flatFile instanceof Yaml))
            return;
        final Yaml data = ((Yaml) flatFile);

        switch (mode) {
            case SET:
                if (framed) {
                    consumer = yaml -> ((Yaml) yaml).framedHeader(Arrays
                            .stream(delta)
                            .map(Object::toString)
                            .toArray(String[]::new));
                } else {
                    consumer = yaml -> ((Yaml) yaml).setHeader(Arrays
                            .stream(delta)
                            .map(Object::toString)
                            .collect(Collectors.toList()));
                }
                break;
            case ADD:
                consumer = yaml -> ((Yaml) yaml).addHeader(Arrays
                        .stream(delta)
                        .map(Object::toString)
                        .toArray(String[]::new));
                break;
            case RESET:
            case DELETE:
            case REMOVE:
            case REMOVE_ALL:
                consumer = yaml -> ((Yaml) yaml).setHeader(new ArrayList<>());
                break;
        }

        if (consumer == null)
            throw new UnsupportedOperationException();
        if (queue == null) {
            consumer.accept(data);
        } else {
            queue.add(consumer);
        }
    }

    @Override
    public @NotNull Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "yaml header";
    }

    @Override
    protected String[] convert(String t) {
        final FlatFile data = Utils.parse(t);
        if (data == null)
            return new String[0];
        if (!(data instanceof Yaml))
            return new String[0];
        return ((Yaml) data).getHeader().toArray(new String[0]);
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expr, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull SkriptParser.ParseResult parseResult) {
        node = SkriptLogger.getNode();
        framed = parseResult.mark == 1;
        return super.init(expr, matchedPattern, isDelayed, parseResult);
    }
}
