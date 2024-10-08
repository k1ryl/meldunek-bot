package k1ryl.meldunekbot.meldunek;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/application/meldunek")
public class MeldunekApplicationController {

    private final MeldunekApplicationGenerator meldunekApplicationGenerator;

    public MeldunekApplicationController(MeldunekApplicationGenerator meldunekApplicationGenerator) {
        this.meldunekApplicationGenerator = meldunekApplicationGenerator;
    }

    public record TestDto(String surname,
                          String name) {
    }

    @PostMapping("/generate")
    public ResponseEntity generateApplication(@RequestBody TestDto request) {
        try {
            byte[] pdf = meldunekApplicationGenerator.generateApplication(request);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=application.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}