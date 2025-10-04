package lmc.configuration.model;

import jakarta.persistence.*;
import lmc.option.model.Option;
import lmc.unit.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configurations")
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String code;

    @Column
    @Enumerated(EnumType.STRING)
    private MachineLine line;

    @Column
    @Enumerated(EnumType.STRING)
    private MachineType type;

    @Column
    private String description;

    @Column
    private String model;

    @OneToMany
    @JoinColumn(name = "configuration_id")
    List<Unit>includedUnits = new ArrayList<>();

    @OneToMany
    @JoinColumn(name ="option_id")
    List<Option>includedOptions = new ArrayList<>();

}
