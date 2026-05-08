package by.shakhau.strpingsecurityjwt.domain.repository;

import by.shakhau.strpingsecurityjwt.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
