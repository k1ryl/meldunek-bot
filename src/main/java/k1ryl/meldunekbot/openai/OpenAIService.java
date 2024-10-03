package k1ryl.meldunekbot.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import k1ryl.meldunekbot.meldunek.ApplicationState;
import k1ryl.meldunekbot.meldunek.dto.ContactDataDto;
import k1ryl.meldunekbot.meldunek.dto.PeselDto;
import k1ryl.meldunekbot.meldunek.dto.UserDataDto;
import k1ryl.meldunekbot.openai.dto.OpenAIResponse;
import k1ryl.meldunekbot.openai.dto.StateData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static k1ryl.meldunekbot.openai.OpenAIPrompts.*;

@Service
@RequiredArgsConstructor
@Slf4j
//todo retry 3 times. after it exception and ask user to try again
public class OpenAIService {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper;

    public StateData<?> extractStateData(String rawStateData, Set<String> fieldsToExtract, ApplicationState state) {
        try {
            Optional<OpenAIResponse> openAIResponse = client.requestJsonCompletion(getStatePrompt(rawStateData, fieldsToExtract, state));
            if (openAIResponse.isPresent()) {
                String jsonStateData = openAIResponse.get().choices().get(0).message().content();
                Class<?> stateDataClass = getStateDataClass(state);
                var stateData = objectMapper.readValue(jsonStateData, stateDataClass);
                return new StateData<>(stateData);
            }
        } catch (Exception e) {
            log.error("Failed to extract state data from OpenAI", e);
            return null;
        }
        return null;
    }

    public PeselDto extractPesel(String rawPesel) {
        try {
            Optional<OpenAIResponse> openAIResponse = client.requestJsonCompletion(extractPeselPrompt(rawPesel));
            if (openAIResponse.isPresent()) {
                String jsonPeselData = openAIResponse.get().choices().get(0).message().content();
                return objectMapper.readValue(jsonPeselData, PeselDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to extract PESEL from OpenAI", e);
            return null;
        }
        return null;
    }

    private String getStatePrompt(String rawStateData, Set<String> fieldsToExtract, ApplicationState state) {
        return switch (state) {
            case PERSONAL_DATA -> extractPersonalDataPrompt(rawStateData, fieldsToExtract);
            case PESEL -> extractPeselPrompt(rawStateData);
            case CONTACT_DATA -> extractContactDataPrompt(rawStateData, fieldsToExtract);
            case APARTMENT_DATA -> extractApartmentDataPrompt(rawStateData, fieldsToExtract);
            default -> throw new IllegalArgumentException("Unsupported state: " + state);
        };
    }

    private Class<?> getStateDataClass(ApplicationState state) {
        return switch (state) {
            case PERSONAL_DATA -> UserDataDto.class;
            case PESEL -> PeselDto.class;
            case CONTACT_DATA -> ContactDataDto.class;
            default -> throw new IllegalArgumentException("Unsupported state: " + state);
        };
    }
}