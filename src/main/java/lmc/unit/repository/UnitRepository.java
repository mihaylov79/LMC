package lmc.unit.repository;

import lmc.unit.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Optional<Unit> findByCode(String code);

    List<Unit> findAllByActiveIs(boolean active);
}
