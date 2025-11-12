package lmc.user.repository;

import lmc.user.model.User;
import lmc.user.model.UserStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findAllByStatus(UserStatus status, Sort sort);

    List<User> findAllByIdIsNot(UUID id, Sort sort);
}
