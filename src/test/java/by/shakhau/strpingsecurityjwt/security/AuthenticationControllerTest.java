package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public class AuthenticationControllerTest {

    private static final String USER_NAME = "user name";
    private static final String PASSWORD = "password";

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test-security-database")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserService userService;

    @BeforeAll
    @Sql(scripts = "classpath:/sql/init.sql")
    public static void startContainer() {
        postgres.start();

        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @Test
    public void shouldLoginUserAndGetSecuredDataAndLogout() {
        User user = userService.createUser(new User(null, USER_NAME, PASSWORD));
        System.out.println(user);
    }
}
