package com.turno.crm.repository;

import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.UserRole;
import com.turno.crm.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    @Query(value = "SELECT * FROM users WHERE status = 'ACTIVE' AND (name ILIKE '%' || :q || '%' OR email ILIKE '%' || :q || '%')",
           nativeQuery = true)
    List<User> search(@Param("q") String query);
}
