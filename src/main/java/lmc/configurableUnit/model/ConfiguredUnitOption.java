package lmc.configurableUnit.model;


import jakarta.persistence.*;
import lmc.option.model.Option;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "configured_unit_options")
public class ConfiguredUnitOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configured_unit_id")
    private ConfiguredUnit configuredUnit;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private Option option;

    @Column(nullable = false)
    private int quantity;


}
