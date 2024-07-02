package k1ryl.meldunekbot.meldunek.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GenerateMeldunekApplication(
        String surname,
        String name,
        String pesel,
        String countryOfBirth,
        LocalDate dateOfBirth,
        String placeOfBirth,
        String countryOfResidence,
        String phone,
        String email,
        String street,
        String houseNumber,
        String flatNumber,
        String postalCode,
        String cityOrCityDistrict,
        String gmina,
        String voivodeship,
        LocalDate periodOfResidenceFrom,
        LocalDate periodOfResidenceTo
) {
}