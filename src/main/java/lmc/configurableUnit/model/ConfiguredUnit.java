package lmc.configurableUnit.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configured_units")
public class ConfiguredUnit extends ConfigurableUnit {


    @Builder.Default
    @OneToMany(mappedBy = "configuredUnit", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderColumn(name = "option_order")

        private List<ConfiguredUnitOption> options = new ArrayList<>();

    public void addOption(ConfiguredUnitOption cuo){
        if (cuo == null) {
            return;
        }
        if (!this.options.contains(cuo)) {
            cuo.setConfiguredUnit(this);
            this.options.add(cuo);
        } else {
            cuo.setConfiguredUnit(this);
        }
    }

    public void removeOption(ConfiguredUnitOption cuo){
        if (cuo == null){
            return;
        }
        if (this.options.remove(cuo)) {
            cuo.setConfiguredUnit(null);
        }
    }




}
