package lmc.configurationUnit.model;

import jakarta.persistence.*;
import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configuration.model.Configuration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configuration_units")
public class ConfigurationUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "configuration_id", nullable = false)
    private Configuration configuration;

    @ManyToOne
    @JoinColumn(name = "configurable_unit_id", nullable = false)
    private ConfigurableUnit configurableUnit;

    @Column(nullable = false)
    private int quantity;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
