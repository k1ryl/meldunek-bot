package k1ryl.meldunekbot.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Command {

    START("/start"),
    MELDUNEK("/meldunek"),
    HELP("/help");

    private final String command;

    public static Command fromText(String text) {
        for (Command command : Command.values()) {
            if (command.command.equalsIgnoreCase(text)) {
                return command;
            }
        }
        throw new IllegalArgumentException();
    }
}