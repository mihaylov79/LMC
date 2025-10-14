package lmc.unit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String imageUrl;

    @Column(unique = true, nullable = false)
    private String code;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String size;

    @Column
    private BigDecimal price;

    @Column
    private CurrencyType currency;

    @Column
    private boolean active;


}
