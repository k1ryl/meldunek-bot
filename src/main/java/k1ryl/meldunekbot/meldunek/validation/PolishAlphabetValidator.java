package k1ryl.meldunekbot.meldunek.validation;

import java.util.regex.Pattern;

public class PolishAlphabetValidator {

    private static final Pattern POLISH_ALPHABET_PATTERN = Pattern.compile("^[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ\\s\\-'.]*$");

    public static boolean isTextPolishCompatible(String text) {
        return POLISH_ALPHABET_PATTERN.matcher(text).matches();
    }
}