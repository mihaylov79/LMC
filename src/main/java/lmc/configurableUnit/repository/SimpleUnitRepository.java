package lmc.configurableUnit.repository;

import lmc.configurableUnit.model.SimpleUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SimpleUnitRepository extends JpaRepository<SimpleUnit, UUID> {

}
