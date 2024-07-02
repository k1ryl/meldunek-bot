package k1ryl.meldunekbot.meldunek.validation;

import jakarta.annotation.Nullable;
import k1ryl.meldunekbot.meldunek.dto.ContactDataDto;
import k1ryl.meldunekbot.meldunek.dto.PeselDto;
import k1ryl.meldunekbot.meldunek.dto.UserDataDto;
import k1ryl.meldunekbot.meldunek.validation.model.FieldStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import pl.unak7.peselvalidator.PeselValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static k1ryl.meldunekbot.meldunek.validation.PolishAlphabetValidator.isTextPolishCompatible;
import static k1ryl.meldunekbot.meldunek.validation.model.FieldStatus.MISSING;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationDataValidator {

    private final PeselValidator peselValidator;

    public Map<String, FieldStatus> validate(UserDataDto userData, @Nullable Set<String> fieldsToValidate) {
        Map<String, FieldStatus> invalidFields = new HashMap<>();
        log.debug("Validating user data {}", userData);

        if ((fieldsToValidate == null || fieldsToValidate.contains("name")) && StringUtils.isBlank(userData.name())) {
            invalidFields.put("name", MISSING);
        } else if (StringUtils.isNotBlank(userData.name()) && !isTextPolishCompatible(userData.name())) {
            invalidFields.put("name", FieldStatus.NOT_POLISH_COMPATIBLE);
        }

        if ((fieldsToValidate == null || fieldsToValidate.contains("surname")) && StringUtils.isBlank(userData.surname())) {
            invalidFields.put("surname", MISSING);
        } else if (StringUtils.isNotBlank(userData.surname()) && !isTextPolishCompatible(userData.surname())) {
            invalidFields.put("surname", FieldStatus.NOT_POLISH_COMPATIBLE);
        }

        if ((fieldsToValidate == null || fieldsToValidate.contains("dateOfBirth")) && userData.dateOfBirth() == null) {
            invalidFields.put("dateOfBirth", MISSING);
        }

        if ((fieldsToValidate == null || fieldsToValidate.contains("countryOfBirth")) && StringUtils.isBlank(userData.countryOfBirth())) {
            invalidFields.put("countryOfBirth", MISSING);
        } else if (StringUtils.isNotBlank(userData.countryOfBirth()) && !isTextPolishCompatible(userData.countryOfBirth())) {
            invalidFields.put("countryOfBirth", FieldStatus.NOT_POLISH_COMPATIBLE);
        }

        if ((fieldsToValidate == null || fieldsToValidate.contains("placeOfBirth")) && StringUtils.isBlank(userData.placeOfBirth())) {
            invalidFields.put("placeOfBirth", MISSING);
        } else if (StringUtils.isNotBlank(userData.placeOfBirth()) && !isTextPolishCompatible(userData.placeOfBirth())) {
            invalidFields.put("placeOfBirth", FieldStatus.NOT_POLISH_COMPATIBLE);
        }

        return invalidFields;
    }

    public Map<String, FieldStatus> validate(PeselDto peselDto) {
        Map<String, FieldStatus> invalidFields = new HashMap<>();

        log.debug("Validating pesel {}", peselDto);
        if (!peselValidator.validate(peselDto.pesel())) {
            invalidFields.put("pesel", FieldStatus.INVALID);
        }

        return invalidFields;
    }

    public Map<String, FieldStatus> validate(ContactDataDto contactDataDto, @Nullable Set<String> fieldsToValidate) {
        Map<String, FieldStatus> invalidFields = new HashMap<>();
        log.debug("Validating contact data {}", contactDataDto);

        if ((fieldsToValidate == null || fieldsToValidate.contains("phone")) && StringUtils.isBlank(contactDataDto.phone())) {
            invalidFields.put("phone", MISSING);
        }

        if ((fieldsToValidate == null || fieldsToValidate.contains("email")) && StringUtils.isBlank(contactDataDto.email())) {
            invalidFields.put("email", MISSING);
        }

        return invalidFields;
    }
}