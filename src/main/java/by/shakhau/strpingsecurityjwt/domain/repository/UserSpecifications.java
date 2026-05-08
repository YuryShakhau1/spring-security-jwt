package by.shakhau.strpingsecurityjwt.domain.repository;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> search(String firstName, String lastName, String email) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (firstName != null && !firstName.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
            }

            if (lastName != null && !lastName.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }

            if (email != null && !email.isEmpty()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
