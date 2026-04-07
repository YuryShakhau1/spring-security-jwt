package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.AuthenticationController.AuthRequest;
import by.shakhau.strpingsecurityjwt.security.AuthenticationController.TokenResponse;
import by.shakhau.strpingsecurityjwt.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ImportAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class
})
public class AuthenticationControllerTest {

    private static final String USER_NAME = "user name";
    private static final String PASSWORD = "password";

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test-security-database")
            .withUsername("test")
            .withPassword("test");

    private WebTestClient webClient;

    @Autowired
    private UserService userService;

    @BeforeAll
    public static void startContainer() {
        postgres.start();

        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @AfterAll
    public static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    public void setup() {
        webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + 8080)
                .build();
    }

    @Test
    public void shouldLoginUserAndGetSecuredDataAndLogout() {
        assertThat(userService.findByName(USER_NAME)).isNull();

        var password = new char[PASSWORD.length()];
        PASSWORD.getChars(0, PASSWORD.length(), password, 0);

        // register new user
        webClient.post().uri("/auth/register")
                .body(BodyInserters.fromValue(new AuthRequest(USER_NAME, password)))
                .exchange()
                .expectStatus().isOk();

        // login user
        TokenResponse token = webClient.post().uri("/auth/login")
                .body(BodyInserters.fromValue(new AuthRequest(USER_NAME, password)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult().getResponseBody();

        assertThat(token).isNotNull();

        // when user is not authorized
        webClient.get().uri("/user/")
                .exchange()
                .expectStatus().isUnauthorized();

        // when user is authorized
        List<User> users = webClient.get().uri("/user/")
                .header("Authorization", "Bearer " + token.getAccessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .returnResult().getResponseBody();

        // logout when user is unauthorized
        webClient.delete().uri("/auth/logout/all")
                .exchange()
                .expectStatus().isUnauthorized();

        // logout when user is authorized
        webClient.delete().uri("/auth/logout/all")
                .header("Authorization", "Bearer " + token.getAccessToken())
                .exchange()
                .expectStatus().isOk();

        assertThat(users.size()).isEqualTo(1);

        User user = users.get(0);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD);
    }
}
