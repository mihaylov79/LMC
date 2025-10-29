package lmc.configurationUnitOption.model;

import jakarta.persistence.*;
import lmc.configurationUnit.model.ConfigurationUnit;
import lmc.option.model.Option;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "configuration_unit_options")
public class ConfigurationUnitOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_unit_id")
    private ConfigurationUnit configurationUnit;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private Option option;

    @Column(nullable = false)
    private int quantity = 1;


}
