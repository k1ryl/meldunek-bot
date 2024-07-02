package k1ryl.meldunekbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.unak7.peselvalidator.PeselValidator;
import pl.unak7.peselvalidator.PeselValidatorImpl;

@Configuration
public class PeselValidatorConfig {

    @Bean
    public PeselValidator peselValidator() {
        return new PeselValidatorImpl();
    }
}