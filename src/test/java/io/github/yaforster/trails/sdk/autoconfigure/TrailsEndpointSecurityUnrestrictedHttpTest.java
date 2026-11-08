package io.github.yaforster.trails.sdk.autoconfigure;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TrailsSecurityTestApplication.class,
        TrailsEndpointSecurityHttpTest.Endpoints.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrailsEndpointSecurityUnrestrictedHttpTest {
    @Value("${local.server.port}")
    private int port;

    @Test
    void shouldRequireEndpointCredentials_whenUserAccessIsUnrestricted() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> user = client.send(request("/api/security-probe").build(),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> anonymous = client.send(request("/agent").build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> authenticated = client.send(request("/agent").header("X-Agent", "first-key")
                                                                          .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(List.of(user.statusCode(), anonymous.statusCode(), authenticated.statusCode(),
                authenticated.body())).containsExactly(200, 401, 200, "first:false");
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                          .GET();
    }
}
