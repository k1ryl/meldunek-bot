package k1ryl.meldunekbot.openai.dto;

import java.util.List;

public record OpenAIRequest(
        String model,
        ResponseFormat response_format,
        List<Message> messages
) {

    public record ResponseFormat(String type) {
    }

    public record Message(String role, String content) {
    }
}