package lmc.configurableUnit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(force = true)
@Entity
@Table(name = "simple_units")
public class SimpleUnit extends ConfigurableUnit {


}
