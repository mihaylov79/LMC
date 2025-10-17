package lmc.option.model;


import jakarta.persistence.*;
import lmc.unit.model.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = "compatible_with")
    @Enumerated(EnumType.STRING)
    private SupportedBy compatibleWith;

    @Column
    private boolean active;

    @Column
    private BigDecimal price;

    @Column
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;
}
