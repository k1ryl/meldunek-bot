package k1ryl.meldunekbot.meldunek.postcode.dto;

public record PostCodeResponse(
        String kod,
        String miejscowosc,
        String ulica,
        String gmina,
        String wojewodztwo,
        String dzielnica
) {
}