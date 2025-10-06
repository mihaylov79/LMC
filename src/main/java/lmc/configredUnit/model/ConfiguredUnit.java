package lmc.configredUnit.model;


import jakarta.persistence.*;
import lmc.option.model.Option;
import lmc.unit.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "configured_units")
public class ConfiguredUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToMany
    @JoinTable(
        name = "configured_unit_options",
        joinColumns = @JoinColumn(name = "configured_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
        private List<Option> options = new ArrayList<>();

    @Column
    private int quantity;


}
