package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.security.TrailsEndpointSecurity;
import jakarta.servlet.http.HttpServletRequest;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {TrailsSecurityTestApplication.class,
        TrailsEndpointSecurityHttpTest.Endpoints.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
                "server.servlet.context-path=/wrapper", "trails.test.authentication.enabled=true",
                "trails.security.roles.test-manager=manager", "trails.security.roles.tester=tester",
                "trails.security.roles.admin=admin", "trails.security.roles.deployment-reporter=reporter"})
class TrailsEndpointSecurityHttpTest {
    @Value("${local.server.port}")
    private int port;

    @ParameterizedTest
    @ValueSource(strings = {"/agent", "/%61gent"})
    void shouldChallengeMissingCredentials_whenContainerDecodesEndpointPath(String path) throws Exception {
        assertThat(get(HttpClient.newHttpClient(), path).statusCode()).isEqualTo(401);
    }

    @Test
    void shouldChallengeRejectedCredentials_whenManagerRejectsToken() throws Exception {
        assertThat(get(HttpClient.newHttpClient(), "/agent", "X-Agent", "invalid").statusCode()).isEqualTo(401);
    }

    @Test
    void shouldChallengeMalformedCredentials_whenConverterRejectsToken() throws Exception {
        assertThat(get(HttpClient.newHttpClient(), "/agent", "X-Agent", "malformed").statusCode()).isEqualTo(401);
    }

    @Test
    void shouldRetainUnauthorizedPolicy_whenAuthenticationServiceFails() throws Exception {
        assertThat(get(HttpClient.newHttpClient(), "/agent", "X-Agent", "unavailable").statusCode()).isEqualTo(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/agent", "/%61gent"})
    void shouldContinueWithEndpointIdentity_whenCredentialsAreValid(String path) throws Exception {
        HttpResponse<String> response = get(HttpClient.newHttpClient(), path, "X-Agent", "first-key");
        assertThat(List.of(response.statusCode(), response.body())).containsExactly(200, "first:false");
    }

    @Test
    void shouldUseEachEndpointsConverterAndManager_whenMultipleEndpointsAreRegistered() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        assertThat(List.of(get(client, "/other", "X-Other", "second-key").body(),
                get(client, "/other", "X-Agent", "first-key").statusCode(),
                get(client, "/other", "X-Other", "first-key").statusCode())).containsExactly("second:false", 401, 401);
    }

    @Test
    void shouldRequireCredentialsOnEveryRequest_whenClientRetainsCookies() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                                      .cookieHandler(new CookieManager())
                                      .build();
        HttpResponse<String> authenticated = get(client, "/agent", "X-Agent", "first-key");
        assertThat(List.of(authenticated.statusCode(), authenticated.headers()
                                                                    .allValues("Set-Cookie"),
                get(client, "/agent").statusCode())).containsExactly(200, List.of(), 401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/security-probe", "/user/security-probe"})
    void shouldRejectEndpointCredentials_whenAccessingUserRoutes(String path) throws Exception {
        assertThat(get(HttpClient.newHttpClient(), path, "X-Agent", "first-key").statusCode()).isEqualTo(401);
    }

    @Test
    void shouldRejectUserCredentials_whenAccessingEndpoint() throws Exception {
        assertThat(
                get(HttpClient.newHttpClient(), "/agent", "Authorization", basicCredentials()).statusCode()).isEqualTo(
                        401);
    }

    @Test
    void shouldIgnoreAndPreserveUserSession_whenAuthenticatingEndpoint() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                                      .cookieHandler(new CookieManager())
                                      .build();
        HttpResponse<String> userSession = get(client, "/user/session-probe", "Authorization", basicCredentials());
        assertThat(
                List.of(userSession.body(), get(client, "/agent").statusCode(),
                        get(client, "/agent", "X-Agent", "first-key").body(),
                        get(client, "/user/session-probe").body())).containsExactly("tester", 401, "first:true",
                                "tester");
    }

    @Test
    void shouldMatchOnlyExactEndpoint_whenRequestUsesLongerPath() throws Exception {
        assertThat(get(HttpClient.newHttpClient(), "/agent/child").statusCode()).isEqualTo(404);
    }

    private HttpResponse<String> get(HttpClient client, String path, String... headers) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/wrapper" + path));
        if (headers.length > 0) {
            request.headers(headers);
        }
        return client.send(request.GET()
                                  .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String basicCredentials() {
        return "Basic " + java.util.Base64.getEncoder()
                                          .encodeToString(
                                                  "tester:password".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @Import(EndpointProbe.class)
    static class Endpoints {
        @Bean
        TrailsEndpointSecurity firstEndpoint() {
            return endpoint("/agent", "X-Agent", "first-key", "first");
        }

        @Bean
        TrailsEndpointSecurity secondEndpoint() {
            return endpoint("/other", "X-Other", "second-key", "second");
        }

        private TrailsEndpointSecurity endpoint(String path, String header, String key, String identity) {
            return new TrailsEndpointSecurity(path, authentication -> {
                if ("unavailable".equals(authentication.getCredentials())) {
                    throw new AuthenticationServiceException("Test provider unavailable");
                }
                if (!key.equals(authentication.getCredentials())) {
                    throw new BadCredentialsException("Rejected test credential");
                }
                return UsernamePasswordAuthenticationToken.authenticated(identity, null, List.of());
            }, request -> {
                String credential = request.getHeader(header);
                if (credential == null) {
                    return null;
                }
                if (credential.equals("malformed")) {
                    throw new BadCredentialsException("Malformed test credential");
                }
                return UsernamePasswordAuthenticationToken.unauthenticated("agent", credential);
            });
        }
    }

    @RestController
    static class EndpointProbe {
        @GetMapping({"/agent", "/other"})
        String endpoint(Authentication authentication, HttpServletRequest request) {
            return authentication.getName() + ":" + (request.getSession(false) != null);
        }

        @GetMapping("/user/session-probe")
        String userSession(Authentication authentication, HttpServletRequest request) {
            request.getSession()
                   .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                           SecurityContextHolder.getContext());
            return authentication.getName();
        }
    }
}
