package lmc.configurableUnit.model;


import jakarta.persistence.*;
import lmc.option.model.Option;
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
    @ManyToMany
    @JoinTable(
        name = "configured_unit_options",
        joinColumns = @JoinColumn(name = "configured_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
        private List<Option> options = new ArrayList<>();




}
