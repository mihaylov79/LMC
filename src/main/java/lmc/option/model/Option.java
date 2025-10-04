package lmc.option.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String code;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = "compatible_with")
    @Enumerated(EnumType.STRING)
    private SupportedBy compatibleWith;

    @Column
    private BigDecimal price;
}
