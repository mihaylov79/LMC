package lmc.offer.model;

import jakarta.persistence.*;
import lmc.company.model.Company;
import lmc.configuration.model.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;


    @ManyToOne
    @JoinColumn(name = "configuration_id")
    private Configuration configuration;

    @Column
    private BigDecimal discount;

    @Column
    private BigDecimal finalPrice;

    @Column
    private LocalDate created;

    @Column
    private LocalDate expires;

    @Column
    private boolean succeeded;


}
