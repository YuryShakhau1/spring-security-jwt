package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.controller.AuthenticationController.LoginRequest;
import by.shakhau.strpingsecurityjwt.controller.AuthenticationController.RegisterRequest;
import by.shakhau.strpingsecurityjwt.controller.AuthenticationController.TokenResponse;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ImportAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class
})
public class AuthenticationControllerTest {

    private static final String USER_NAME = "user name";
    private static final String USER_LAST_NAME = "user last name";
    private static final String EMAIL = "user email";
    private static final String PASSWORD = "password";

    @Container
    @ServiceConnection
    private static final MariaDBContainer<?> container = new MariaDBContainer<>(
            "mariadb:10.11");

    private WebTestClient webClient;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + 8080)
                .build();
    }

    @Test
    public void shouldLoginUserAndGetSecuredDataAndLogout() {
        assertThat(userService.findByEmail(USER_NAME)).isNull();

        var password = new char[PASSWORD.length()];
        PASSWORD.getChars(0, PASSWORD.length(), password, 0);

        // register new user
        webClient.post().uri("/auth/register")
                .body(BodyInserters.fromValue(new RegisterRequest(
                        USER_NAME, USER_LAST_NAME, EMAIL, password)))
                .exchange()
                .expectStatus().isOk();

        // login user
        TokenResponse token = webClient.post().uri("/auth/login")
                .body(BodyInserters.fromValue(new LoginRequest(EMAIL, password)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult().getResponseBody();

        assertThat(token).isNotNull();

        // when user is not authorized
        webClient.get().uri("/users/me")
                .exchange()
                .expectStatus().isUnauthorized();

        // when user is authorized
        User user = webClient.get().uri("/users/me")
                .header("Authorization", "Bearer " + token.getAccessToken())
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
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

        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.getLastName()).isEqualTo(USER_LAST_NAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD);
    }
}
