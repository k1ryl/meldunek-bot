package k1ryl.meldunekbot.openai;

import java.util.Set;

public class OpenAIPrompts {

    static final String EXTRACT_PERSONAL_DATA = """
            User provided message with user's data:
            %s
            Extract needed data and format it to the json format below.
            Do not guess data unless it is explicitly stated in the message.
            %s
            JSON:%s
            If some data is non-extractable or not present, leave it as null""";

    static final String EXTRACT_PESEL = """
            User was asked: "Do you have a PESEL number? If yes, please provide it."
            User's answer: "%s"
            Extract the PESEL number from the user's answer and format it to the json format below.
            If user explicitly answered that he have no PESEL set the field as "NO". In other cases leave it as null.
            JSON:
            {
                "pesel": String
            }""";

    static final String EXTRACT_CONTACT_DATA = """
            User provided message with contact data:
            %s
            Extract email and phone and format it to the json format below.
            Do not guess data unless it is explicitly stated in the message.
            JSON:%s
            If some data is non-extractable or not present, leave it as null""";

    public static String extractPersonalDataPrompt(String rawUserData, Set<String> fieldsToExtract) {
        StringBuilder fieldsJson = new StringBuilder("{\n");
        for (String field : fieldsToExtract) {
            if (field.equals("dateOfBirth")) {
                fieldsJson.append(String.format("\t\"%s\": LocalDate,\n", field));
            } else {
                fieldsJson.append(String.format("\t\"%s\": String,\n", field));
            }
        }
        fieldsJson.append("}");

        String translationInstruction = (fieldsToExtract.contains("countryOfBirth") || fieldsToExtract.contains("placeOfBirth")) ?
                "Translate countryOfBirth and placeOfBirth to Polish if those fields are provided.\n" : "";

        return String.format(EXTRACT_PERSONAL_DATA, rawUserData, translationInstruction, fieldsJson);
    }

    public static String extractPeselPrompt(String userAnswer) {
        return String.format(EXTRACT_PESEL, userAnswer);
    }

    public static String extractContactDataPrompt(String rawContactData, Set<String> fieldsToExtract) {
        StringBuilder fieldsJson = new StringBuilder("{\n");
        for (String field : fieldsToExtract) {
            fieldsJson.append(String.format("\t\"%s\": String,\n", field));
        }
        fieldsJson.append("}");

        return String.format(EXTRACT_CONTACT_DATA, rawContactData, fieldsJson);
    }
}