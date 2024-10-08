package k1ryl.meldunekbot.meldunek;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class MeldunekApplicationGenerator {

    public static void main(String[] args) throws IOException {
        String pdfPath = MeldunekApplicationGenerator.class.getClassLoader().getResource("meldunek.pdf").getPath();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfReader reader = new PdfReader(pdfPath);
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(reader, writer)) {

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            Map<String, PdfFormField> fields = form.getAllFormFields();

            fields.get("nazwisko").setValue("DOE");
            fields.get("imię (imiona)").setValue("JOHN");
            fields.get("kraj urodzenia").setValue("USA");

            form.flattenFields();
        }

        // The filled PDF is now in baos
        byte[] filledPdf = baos.toByteArray();

        // Save the filled PDF to the project root directory under /documents
        String outputPath = Paths.get("documents", "filled_meldunek.pdf").toString();
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(filledPdf);
        }
    }

    public byte[] generateApplication(MeldunekApplicationController.TestDto request) throws IOException {
        String pdfPath = MeldunekApplicationGenerator.class.getClassLoader().getResource("meldunek.pdf").getPath();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfReader reader = new PdfReader(pdfPath);
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(reader, writer)) {

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            Map<String, PdfFormField> fields = form.getAllFormFields();

            fields.get("nazwisko").setValue(request.surname());
            fields.get("imię (imiona)").setValue(request.name());

            form.flattenFields();
        }

        return baos.toByteArray();
    }


//    public byte[] generateApplication(GenerateMeldunekApplication request) {
//        try {
//            String pdfPath = getClass().getClassLoader().getResource("meldunek.pdf").getPath();
//            PdfReader reader = new PdfReader(pdfPath);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            PdfStamper stamper = new PdfStamper(reader, baos);
//            AcroFields fields = stamper.getAcroFields();
//
//            // 1. person details
//            fields.setField("nazwisko", request.surname().toUpperCase());
//            fields.setField("imię (imiona)", request.name().toUpperCase());
//            setPeselNumber(fields, request.pesel());
//            fields.setField("kraj urodzenia", request.countryOfBirth().toUpperCase());
//            setDateOfBirth(fields, request.dateOfBirth());
//            fields.setField("miejsce urodzenia", request.placeOfBirth().toUpperCase());
//            fields.setField("kraj miejsca zamieszkania", request.countryOfResidence().toUpperCase());
//
//            // 2. contact details
//            setPhone(fields, request.phone());
//            fields.setField("adres poczty elektronicznej", request.email().toUpperCase());
//            fields.setField("wyrażam zgodę na przekazanie do rejestru danych kontaktowych imienia, nazwiska, numeru PESEL oraz", "Tak");
//            fields.setField("wyrażam zgodę na przekazanie do rejestru danych kontaktowych numeru telefonu komórkowego", "Tak");
//            fields.setField("wyrażam zgodę na przekazanie do rejestru danych kontaktowych adresu poczty elektronicznej", "Tak");
//
//            // 3. residence details
//            fields.setField("ulica", request.street().toUpperCase());
//            setHouseNumber(fields, request.houseNumber());
//            setFlatNumber(fields, request.flatNumber());
//            setPostalCode(fields, request.postalCode()); //postal code comes in format XX-XXX
//            fields.setField("miejscowość dzielnica", request.cityOrCityDistrict().toUpperCase());
//            fields.setField("gmina", request.gmina().toUpperCase());
//            fields.setField("województwo", request.voivodeship().toUpperCase());
//            setPeriodOfResidenceFrom(fields, request.periodOfResidenceFrom());
//            setPeriodOfResidenceTo(fields, request.periodOfResidenceTo());
//
//            // 6. signatures
//            fields.setField("oświadczenie właściciela miejscowość", request.cityOrCityDistrict().toUpperCase());
//            fields.setField("oświadczenie osoby zgłaszającej miejscowość", request.cityOrCityDistrict().toUpperCase());
//
//            stamper.setFormFlattening(true);
//            stamper.close();
//            reader.close();
//
//            return baos.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public void setPeselNumber(AcroFields fields, String pesel) throws IOException, DocumentException {
//        String[] polishNumbers = {"pierwsza", "druga", "trzecia", "czwarta", "piąta", "szósta", "siódma", "ósma", "dziewiąta", "dziesiąta", "jedenasta"};
//        char[] peselDigits = pesel.toCharArray();
//        for (int i = 0; i < peselDigits.length; i++) {
//            String fieldName = polishNumbers[i] + " cyfra PESEL";
//            fields.setField(fieldName, String.valueOf(peselDigits[i]));
//        }
//    }
//
//    public void setDateOfBirth(AcroFields fields, LocalDate dateOfBirth) throws IOException, DocumentException {
//        String[] polishNumbersDay = {"data urodzenia dzień pierwsza cyfra", "data urodzenia dzień druga cyfra"};
//        String[] polishNumbersMonth = {"data urodzenia miesiąc pierwsza cyfra", "data urodzenia miesiąc druga cyfra"};
//        String[] polishNumbersYear = {"data urodzenia rok pierwsza cyfra", "data urodzenia rok druga cyfra", "data urodzenia rok trzecia cyfra", "data urodzenia rok czwarta cyfra"};
//
//        String day = String.format("%02d", dateOfBirth.getDayOfMonth());
//        String month = String.format("%02d", dateOfBirth.getMonthValue());
//        String year = String.valueOf(dateOfBirth.getYear());
//
//        for (int i = 0; i < day.length(); i++) {
//            fields.setField(polishNumbersDay[i], String.valueOf(day.charAt(i)));
//        }
//
//        for (int i = 0; i < month.length(); i++) {
//            fields.setField(polishNumbersMonth[i], String.valueOf(month.charAt(i)));
//        }
//
//        for (int i = 0; i < year.length(); i++) {
//            fields.setField(polishNumbersYear[i], String.valueOf(year.charAt(i)));
//        }
//    }
//
//    public void setPhone(AcroFields fields, String phone) throws IOException, DocumentException {
//        String[] polishNumbersPhone = {
//                "pierwsza cyfra numeru telefonu komórkowego",
//                "druga cyfra numeru telefonu komórkowego",
//                "trzecia cyfra numeru telefonu komórkowego",
//                "czwarta cyfra numeru telefonu komórkowego",
//                "piąta cyfra numeru telefonu komórkowego",
//                "szósta cyfra numeru telefonu komórkowego",
//                "siódma cyfra numeru telefonu komórkowego",
//                "ósma cyfra numeru telefonu komórkowego",
//                "dziewiąta cyfra numeru telefonu komórkowego",
//                "dziesiąta cyfra numeru telefonu komórkowego",
//                "jedenasta cyfra numeru telefonu komórkowego",
//                "dwunasta cyfra numeru telefonu komórkowego",
//                "trzynasta cyfra numeru telefonu komórkowego",
//                "czternasta cyfra numeru telefonu komórkowego",
//                "piętnasta cyfra numeru telefonu komórkowego",
//                "szesnasta cyfra numeru telefonu komórkowego",
//                "siedemansta cyfra numeru telefonu komórkowego",
//                "osiemnasta cyfra numeru telefonu komórkowego",
//                "dziewiętnasta cyfra numeru telefonu komórkowego",
//                "dwudziesta cyfra numeru telefonu komórkowego"
//        };
//
//        char[] phoneDigits = phone.toCharArray();
//        for (int i = 0; i < phoneDigits.length; i++) {
//            fields.setField(polishNumbersPhone[i], String.valueOf(phoneDigits[i]));
//        }
//    }
//
//    public void setHouseNumber(AcroFields fields, String houseNumber) throws IOException, DocumentException {
//        String[] polishNumbersHouse = {
//                "numer domu pierwsza cyfra",
//                "numer domu druga cyfra",
//                "numer domu trzecia cyfra",
//                "numer domu czwarta cyfra",
//                "numer domu piąta cyfra",
//                "numer domu szósta cyfra",
//                "numer domu siódma cyfra",
//                "numer domu ósma cyfra",
//                "numer domu dziewiąta cyfra"
//        };
//
//        char[] houseNumberDigits = houseNumber.toCharArray();
//        for (int i = 0; i < houseNumberDigits.length; i++) {
//            fields.setField(polishNumbersHouse[i], String.valueOf(houseNumberDigits[i]));
//        }
//    }
//
//    public void setFlatNumber(AcroFields fields, String flatNumber) throws IOException, DocumentException {
//        String[] polishNumbersFlat = {
//                "numer lokalu pierwsza cyfra",
//                "numer lokalu druga cyfra",
//                "numer lokalu trzecia cyfra",
//                "numer lokalu czwarta cyfra",
//                "numer lokalu piąta cyfra",
//                "numer lokalu szósta cyfra",
//                "numer lokalu siódma cyfra"
//        };
//
//        char[] flatNumberDigits = flatNumber.toCharArray();
//        for (int i = 0; i < flatNumberDigits.length; i++) {
//            fields.setField(polishNumbersFlat[i], String.valueOf(flatNumberDigits[i]));
//        }
//    }
//
//    public void setPostalCode(AcroFields fields, String postalCode) throws IOException, DocumentException {
//        String[] polishNumbersPostalCode = {
//                "kod pocztowy pierwsza cyfra",
//                "kod pocztowy druga cyfra",
//                "kod pocztowy trzecia cyfra",
//                "kod pocztowy czwarta cyfra",
//                "kod pocztowy piąta cyfra"
//        };
//
//        String postalCodeWithoutDash = postalCode.replace("-", "");
//        char[] postalCodeDigits = postalCodeWithoutDash.toCharArray();
//        for (int i = 0; i < postalCodeDigits.length; i++) {
//            fields.setField(polishNumbersPostalCode[i], String.valueOf(postalCodeDigits[i]));
//        }
//    }
//
//    public void setPeriodOfResidenceFrom(AcroFields fields, LocalDate periodOfResidenceFrom) throws IOException, DocumentException {
//        String[] polishNumbersPeriodFrom = {
//                "data początkowa dzień pierwsza cyfra",
//                "data początkowa dzień druga cyfra",
//                "data początkowa miesiąc pierwsza cyfra",
//                "data początkowa miesiąć druga cyfra",
//                "data początkowa rok pierwsza cyfra",
//                "data początkowa rok druga cyfra",
//                "data początkowa rok trzecia cyfra",
//                "data początkowa rok czwarta cyfra"
//        };
//
//        String day = String.format("%02d", periodOfResidenceFrom.getDayOfMonth());
//        String month = String.format("%02d", periodOfResidenceFrom.getMonthValue());
//        String year = String.valueOf(periodOfResidenceFrom.getYear());
//
//        fields.setField(polishNumbersPeriodFrom[0], String.valueOf(day.charAt(0)));
//        fields.setField(polishNumbersPeriodFrom[1], String.valueOf(day.charAt(1)));
//        fields.setField(polishNumbersPeriodFrom[2], String.valueOf(month.charAt(0)));
//        fields.setField(polishNumbersPeriodFrom[3], String.valueOf(month.charAt(1)));
//        fields.setField(polishNumbersPeriodFrom[4], String.valueOf(year.charAt(0)));
//        fields.setField(polishNumbersPeriodFrom[5], String.valueOf(year.charAt(1)));
//        fields.setField(polishNumbersPeriodFrom[6], String.valueOf(year.charAt(2)));
//        fields.setField(polishNumbersPeriodFrom[7], String.valueOf(year.charAt(3)));
//    }
//
//    public void setPeriodOfResidenceTo(AcroFields fields, LocalDate periodOfResidenceTo) throws IOException, DocumentException {
//        String[] polishNumbersPeriodTo = {
//                "data końcowa dzień pierwsza cyfra",
//                "data końcowa dzień druga cyfra",
//                "data końcowa miesiąc pierwsza cyfra",
//                "data końcowa miesiąc druga cyfra",
//                "data końcowa rok pierwsza cyfra",
//                "data końcowa rok druga cyfra",
//                "data końcowa rok trzecia cyfra",
//                "data końcowa rok czwarta cyfra"
//        };
//
//        String day = String.format("%02d", periodOfResidenceTo.getDayOfMonth());
//        String month = String.format("%02d", periodOfResidenceTo.getMonthValue());
//        String year = String.valueOf(periodOfResidenceTo.getYear());
//
//        fields.setField(polishNumbersPeriodTo[0], String.valueOf(day.charAt(0)));
//        fields.setField(polishNumbersPeriodTo[1], String.valueOf(day.charAt(1)));
//        fields.setField(polishNumbersPeriodTo[2], String.valueOf(month.charAt(0)));
//        fields.setField(polishNumbersPeriodTo[3], String.valueOf(month.charAt(1)));
//        fields.setField(polishNumbersPeriodTo[4], String.valueOf(year.charAt(0)));
//        fields.setField(polishNumbersPeriodTo[5], String.valueOf(year.charAt(1)));
//        fields.setField(polishNumbersPeriodTo[6], String.valueOf(year.charAt(2)));
//        fields.setField(polishNumbersPeriodTo[7], String.valueOf(year.charAt(3)));
//    }
}