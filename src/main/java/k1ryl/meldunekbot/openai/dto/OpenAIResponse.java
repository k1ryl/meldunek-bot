package k1ryl.meldunekbot.openai.dto;

import java.util.List;

public record OpenAIResponse(
        String id,
        String object,
        Long created,
        String model,
        String system_fingerprint,
        List<Choice> choices,
        Usage usage) {

    public record Choice(
            Integer index,
            Message message,
            Object logprobs,
            String finish_reason) {

        public record Message(String role, String content) {
        }
    }

    public record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {
    }
}