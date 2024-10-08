package k1ryl.meldunekbot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthcheckController {

    @GetMapping("/")
    public String healthcheck() {
        return "OK";
    }
}