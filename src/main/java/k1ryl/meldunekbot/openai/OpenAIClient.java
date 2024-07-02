package k1ryl.meldunekbot.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import k1ryl.meldunekbot.openai.dto.OpenAIRequest;
import k1ryl.meldunekbot.openai.dto.OpenAIRequest.ResponseFormat;
import k1ryl.meldunekbot.openai.dto.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OpenAIClient {

    private static final String MODEL = "gpt-3.5-turbo";
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api-key}")
    private String openaiApiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<OpenAIResponse> requestJsonCompletion(String message) {
        try {
            var requestBody = objectMapper.writeValueAsString(createRequest(message, OpenAIResponseFormat.json_object));
            log.debug("OpenAI request: {}", requestBody);
            var request = new HttpEntity<>(requestBody, getHeaders());

            var response = restTemplate.exchange(
                    COMPLETIONS_URL,
                    HttpMethod.POST,
                    request,
                    OpenAIResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("OpenAI response: {}", response.getBody());
                return Optional.of(response.getBody());
            }
            log.error("Failed to request OpenAI completion, response {}", response);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Failed to request OpenAI completion", ex);
            return Optional.empty();
        }
    }

    private OpenAIRequest createRequest(String message) {
        return createRequest(message, OpenAIResponseFormat.text);
    }

    private OpenAIRequest createRequest(String message, OpenAIResponseFormat responseFormat) {
        var messageDto = new OpenAIRequest.Message("user", message);
        return new OpenAIRequest(MODEL, new ResponseFormat(responseFormat.name()), List.of(messageDto));
    }


    private HttpHeaders getHeaders() {
        final var headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(openaiApiKey));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}