package lmc.configuration.repository;

import lmc.configuration.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {

    @Query("select distinct c from Configuration c " +
            "left join fetch c.includedUnits iu " +
            "left join fetch iu.configurableUnit cu " +
            "left join fetch cu.unit u")
    List<Configuration> findAllWithUnits();

    @Query("select c from Configuration c " +
            "left join fetch c.includedUnits iu " +
            "left join fetch iu.configurableUnit cu " +
            "left join fetch cu.unit u " +
            "where c.id = :id")
    Optional<Configuration> findByIdWithUnits(@Param("id") UUID id);

}
