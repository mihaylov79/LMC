package lmc.configurationUnit.model;

import jakarta.persistence.*;
import lmc.configurableUnit.model.ConfigurableUnit;
import lmc.configuration.model.Configuration;
import lmc.configurationUnitOption.model.ConfigurationUnitOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
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
    private int quantity = 1;

    @Builder.Default
    @OneToMany(mappedBy = "configurationUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfigurationUnitOption> options = new ArrayList<>();

    public void addOption(ConfigurationUnitOption cuo) {
        if (cuo == null) return;
        cuo.setConfigurationUnit(this);
        this.options.add(cuo);
    }


    public void removeOption(ConfigurationUnitOption cuo) {
        if (cuo == null) return;
        if (this.options.remove(cuo)) {
            cuo.setConfigurationUnit(null);
        }
    }



    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
