package k1ryl.meldunekbot.meldunek.postcode;

import k1ryl.meldunekbot.meldunek.postcode.dto.PostCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostCodeService {

    private final RestTemplate restTemplate = new RestTemplate();

    public PostCodeResponse sendRequest(String postCode) {
        String url = "http://kodpocztowy.intami.pl/api/" + postCode;
        ResponseEntity<PostCodeResponse> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, PostCodeResponse.class);

        if (responseEntity.getStatusCode().isError()) {
            log.error("Failed to get post code data. Status code: {}", responseEntity.getStatusCode());
            throw new IllegalStateException("Failed to get post code data");
        }

        return responseEntity.getBody();
    }


}
