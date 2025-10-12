package lmc.configuration.model;

import jakarta.persistence.*;
import lmc.configurableUnit.model.ConfigurableUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "configurations")
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "img_url")
    private String imageUrl;

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

    @Column
    private boolean active;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "price_update_date")
    private LocalDate priceUpdateDate;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "configuration_id")
    private List<ConfigurableUnit>includedUnits = new ArrayList<>();

}
