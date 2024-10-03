package k1ryl.meldunekbot.meldunek;

import jakarta.transaction.Transactional;
import k1ryl.meldunekbot.meldunek.dto.ContactDataDto;
import k1ryl.meldunekbot.meldunek.dto.UserDataDto;
import k1ryl.meldunekbot.meldunek.validation.ApplicationDataValidator;
import k1ryl.meldunekbot.meldunek.validation.model.FieldStatus;
import k1ryl.meldunekbot.openai.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final Map<ApplicationState, Set<String>> STATE_FIELDS = Map.of(
            ApplicationState.PERSONAL_DATA, Set.of("name", "surname", "dateOfBirth", "countryOfBirth", "placeOfBirth"),
            ApplicationState.CONTACT_DATA, Set.of("phone", "email"),
            ApplicationState.CONTACT_DATA, Set.of("phone", "email"),
            ApplicationState.APARTMENT_DATA, Set.of("street", "buildingNumber", "apartmentNumber", "city", "postalCode")
    );

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

    public Map<String, FieldStatus> extractAndSavePersonalData(String message, Application application) {
        Set<String> fieldsToExtract = getFieldsToExtract(application);
        var extractedUserData = (UserDataDto) openAIService.extractStateData(message, fieldsToExtract, application.getState()).getData();

        Map<String, FieldStatus> invalidFields = dataValidator.validate(extractedUserData, fieldsToExtract);
        if (!invalidFields.isEmpty()) {
            log.debug("Personal data is incomplete. Invalid fields: {}", invalidFields);
            application.setStateDetails(invalidFields);
        } else {
            application.setState(ApplicationState.PESEL);
            application.setStateDetails(null);
        }

        // only save valid fields that are in the fieldsToExtract set to not overwrite valid data with null
        if (!invalidFields.containsKey("name") && (fieldsToExtract.contains("name"))) {
            application.setName(extractedUserData.name());
        }
        if (!invalidFields.containsKey("surname") && (fieldsToExtract.contains("surname"))) {
            application.setSurname(extractedUserData.surname());
        }
        if (!invalidFields.containsKey("dateOfBirth") && (fieldsToExtract.contains("dateOfBirth"))) {
            application.setDateOfBirth(extractedUserData.dateOfBirth());
        }
        if (!invalidFields.containsKey("countryOfBirth") && (fieldsToExtract.contains("countryOfBirth"))) {
            application.setCountryOfBirth(extractedUserData.countryOfBirth());
        }
        if (!invalidFields.containsKey("placeOfBirth") && (fieldsToExtract.contains("placeOfBirth"))) {
            application.setPlaceOfBirth(extractedUserData.placeOfBirth());
        }

        repository.save(application);
        return invalidFields;
    }

    public Map<String, FieldStatus> extractAndSavePesel(String message, Application application) {
        var peselDto = openAIService.extractPesel(message);
        log.debug("Extracted PESEL: {}", peselDto.pesel());
        Map<String, FieldStatus> invalidFields = dataValidator.validate(peselDto);
        if (!invalidFields.isEmpty()) {
            log.debug("PESEL {} is invalid", peselDto.pesel());
            application.setStateDetails(invalidFields);
        } else {
            application.setPesel(peselDto.pesel());
            application.setState(ApplicationState.CONTACT_DATA);
            application.setStateDetails(null);
        }
        repository.save(application);
        return invalidFields;
    }

    public Map<String, FieldStatus> extractAndSaveContactData(String message, Application application) {
        Set<String> fieldsToExtract = getFieldsToExtract(application);
        var extractedContactData = (ContactDataDto) openAIService.extractStateData(message, fieldsToExtract, application.getState()).getData();

        Map<String, FieldStatus> invalidFields = dataValidator.validate(extractedContactData, fieldsToExtract);
        if (!invalidFields.isEmpty()) {
            log.debug("Contact data is incomplete. Invalid fields: {}", invalidFields);
            application.setStateDetails(invalidFields);
        } else {
            application.setState(ApplicationState.APARTMENT_DATA);
            application.setStateDetails(null);
        }

        // only save valid fields that are in the fieldsToExtract set to not overwrite valid data with null
        if (!invalidFields.containsKey("phone") && (fieldsToExtract.contains("phone"))) {
            application.setPhone(extractedContactData.phone().replace(" ", ""));
        }
        if (!invalidFields.containsKey("email") && (fieldsToExtract.contains("email"))) {
            application.setEmail(extractedContactData.email());
        }

        repository.save(application);
        return invalidFields;
    }

//    public Map<String, FieldStatus> extractAndSaveApartmentDetailsData(String message, Application application) throws JsonProcessingException {
//        Set<String> fieldsToExtract = null;
//        if (!application.getStateDetails().isEmpty()) {
//            fieldsToExtract = application.getStateDetails().keySet();
//        }
//
//        return extractContactFieldsAndSaveValid(message, fieldsToExtract, application);
//    }

    /**
     * Get fields to extract based on the current state of the application. If the state details are empty, return all fields for the state.
     */
    private Set<String> getFieldsToExtract(Application application) {
        return (application.getStateDetails() == null || application.getStateDetails().isEmpty())
                ? STATE_FIELDS.get(application.getState())
                : application.getStateDetails().keySet();
    }
}