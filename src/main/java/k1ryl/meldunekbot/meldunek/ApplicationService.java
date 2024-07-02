package k1ryl.meldunekbot.meldunek;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import k1ryl.meldunekbot.meldunek.validation.ApplicationDataValidator;
import k1ryl.meldunekbot.meldunek.validation.model.FieldStatus;
import k1ryl.meldunekbot.openai.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository repository;
    private final OpenAIService openAIService;
    private final ApplicationDataValidator dataValidator;
    private final ObjectMapper objectMapper;

    @Transactional
    public Application createNewApplication(Long tgUserId, ApplicationState status) {
        repository.deleteAllByTgUserId(tgUserId); //todo think about payments. can't remove paid application. or introduce smth like balance
        Application application = new Application();
        application.setState(status);
        application.setTgUserId(tgUserId);
        return repository.save(application);
    }

    public Application getLatestApplicationRequestByTgUserId(Long tgUserId) {
        Optional<Application> application = repository.findByTgUserIdOrderByUpdatedAtDesc(tgUserId);
        if (application.isPresent()) {
            return application.get();
        } else {
            throw new IllegalStateException("No application found for TG user %s" + tgUserId);
        }
    }

    public Map<String, FieldStatus> extractAndSavePersonalData(String messageText, Application application) throws JsonProcessingException {
        Set<String> fieldsToExtract = null;
        if (StringUtils.isNotBlank(application.getStateDetails())) {
            fieldsToExtract = objectMapper.readValue(application.getStateDetails(), new TypeReference<Map<String, FieldStatus>>() {
            }).keySet();
        }

        return extractPersonalFieldsAndSaveValid(messageText, fieldsToExtract, application);
    }

    public Map<String, FieldStatus> extractAndSavePesel(String messageText, Application application) throws JsonProcessingException {
        var peselDto = openAIService.extractPesel(messageText);
        log.debug("Extracted PESEL: {}", peselDto.pesel());
        Map<String, FieldStatus> invalidFields = dataValidator.validate(peselDto);
        if (!invalidFields.isEmpty()) {
            log.debug("PESEL {} is invalid", peselDto.pesel());
            application.setStateDetails(objectMapper.writeValueAsString(invalidFields));
        } else {
            application.setPesel(peselDto.pesel());
            application.setState(ApplicationState.CONTACT_DATA);
            application.setStateDetails(null);
        }
        repository.save(application);
        return invalidFields;
    }

    public Map<String, FieldStatus> extractAndSaveContactData(String messageText, Application application) throws JsonProcessingException {
        Set<String> fieldsToExtract = null;
        if (StringUtils.isNotBlank(application.getStateDetails())) {
            fieldsToExtract = objectMapper.readValue(application.getStateDetails(), new TypeReference<Map<String, FieldStatus>>() {
            }).keySet();
        }

        return extractContactFieldsAndSaveValid(messageText, fieldsToExtract, application);
    }


    public Map<String, FieldStatus> extractAndSaveApartmentDetailsData(String messageText, Application application) throws JsonProcessingException {
        Set<String> fieldsToExtract = null;
        if (StringUtils.isNotBlank(application.getStateDetails())) {
            fieldsToExtract = objectMapper.readValue(application.getStateDetails(), new TypeReference<Map<String, FieldStatus>>() {
            }).keySet();
        }

        return extractContactFieldsAndSaveValid(messageText, fieldsToExtract, application);
    }

    private Map<String, FieldStatus> extractPersonalFieldsAndSaveValid(String messageText, @Nullable Set<String> fieldsToExtract, Application application) throws JsonProcessingException {
        var extractedUserData = (fieldsToExtract == null) ? openAIService.extractPersonalData(messageText) : openAIService.extractPersonalData(messageText, fieldsToExtract);
        Map<String, FieldStatus> invalidFields = dataValidator.validate(extractedUserData, fieldsToExtract);
        if (!invalidFields.isEmpty()) {
            log.debug("Personal data is incomplete. Invalid fields: {}", invalidFields);
            application.setStateDetails(objectMapper.writeValueAsString(invalidFields));
        } else {
            application.setState(ApplicationState.PESEL);
            application.setStateDetails(null);
        }

        // Only save valid fields
        if (!invalidFields.containsKey("name") && (fieldsToExtract == null || fieldsToExtract.contains("name"))) {
            application.setName(extractedUserData.name());
        }
        if (!invalidFields.containsKey("surname") && (fieldsToExtract == null || fieldsToExtract.contains("surname"))) {
            application.setSurname(extractedUserData.surname());
        }
        if (!invalidFields.containsKey("dateOfBirth") && (fieldsToExtract == null || fieldsToExtract.contains("dateOfBirth"))) {
            application.setDateOfBirth(extractedUserData.dateOfBirth());
        }
        if (!invalidFields.containsKey("countryOfBirth") && (fieldsToExtract == null || fieldsToExtract.contains("countryOfBirth"))) {
            application.setCountryOfBirth(extractedUserData.countryOfBirth());
        }
        if (!invalidFields.containsKey("placeOfBirth") && (fieldsToExtract == null || fieldsToExtract.contains("placeOfBirth"))) {
            application.setPlaceOfBirth(extractedUserData.placeOfBirth());
        }

        repository.save(application);
        return invalidFields;
    }

    private Map<String, FieldStatus> extractContactFieldsAndSaveValid(String messageText, @Nullable Set<String> fieldsToExtract, Application application) throws JsonProcessingException {
        var extractedContactData = (fieldsToExtract == null) ? openAIService.extractContactData(messageText) : openAIService.extractContactData(messageText, fieldsToExtract);
        Map<String, FieldStatus> invalidFields = dataValidator.validate(extractedContactData, fieldsToExtract);
        if (!invalidFields.isEmpty()) {
            log.debug("Contact data is incomplete. Invalid fields: {}", invalidFields);
            application.setStateDetails(objectMapper.writeValueAsString(invalidFields));
        } else {
            application.setState(ApplicationState.APARTMENT_DATA);
            application.setStateDetails(null);
        }

        // Only save valid fields
        if (!invalidFields.containsKey("phone") && (fieldsToExtract == null || fieldsToExtract.contains("phone"))) {
            application.setPhone(extractedContactData.phone().replace(" ", ""));
        }
        if (!invalidFields.containsKey("email") && (fieldsToExtract == null || fieldsToExtract.contains("email"))) {
            application.setEmail(extractedContactData.email());
        }

        repository.save(application);
        return invalidFields;
    }
}