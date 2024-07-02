package k1ryl.meldunekbot.meldunek;

import k1ryl.meldunekbot.meldunek.dto.GenerateMeldunekApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    private final ApplicationGenerator applicationGenerator;

    public ApplicationController(ApplicationGenerator applicationGenerator) {
        this.applicationGenerator = applicationGenerator;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateApplication(@RequestBody GenerateMeldunekApplication request) {
        byte[] pdf = applicationGenerator.generateApplication(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=application.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}