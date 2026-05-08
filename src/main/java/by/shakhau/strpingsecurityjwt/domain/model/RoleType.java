package by.shakhau.strpingsecurityjwt.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleType {
    SUPER_ADMIN("ROLE_SUPER_ADMIN", 1),
    ADMIN("ROLE_SUPER_ADMIN", 2),
    USER("ROLE_USER", 3);

    public static final Long USER_ROLE_ID = 3L;

    private final String name;
    private final Integer priority;
}
