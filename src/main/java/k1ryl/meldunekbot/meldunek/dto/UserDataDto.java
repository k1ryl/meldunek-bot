package k1ryl.meldunekbot.meldunek.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UserDataDto(
        String name,
        String surname,
        LocalDate dateOfBirth,
        String countryOfBirth,
        String placeOfBirth
) {
}