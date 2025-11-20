package lmc.offer.model;

import jakarta.persistence.*;
import lmc.company.model.Company;
import lmc.configuration.model.Configuration;
import lmc.unit.model.CurrencyType;
import lmc.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, name = "offer_number")
    private String offerNumber;


    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;


    @ManyToOne
    @JoinColumn(name = "configuration_id")
    private Configuration configuration;

    @Column(name = "configuration_price")
    private BigDecimal configurationPrice;

    // JSON snapshot на конфигурацията към момента на създаване на офертата
    // Запазва код, описание, модел, единици, опции и цени
    @Column(name = "configuration_snapshot", columnDefinition = "TEXT")
    private String configurationSnapshot;

    @Column(name = "installation_fee")
    private BigDecimal installationFee;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    @Column(name = "installation_Materials")
    private BigDecimal installationMaterials;

    @Column
    private CurrencyType currency;

    @Column
    private BigDecimal discount;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Column
    private LocalDate created;

    @Column
    private LocalDate expires;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column
    @Enumerated(EnumType.STRING)
    @Setter
    private OfferStatus status;

    @Column
    @Setter
    private boolean deleted;


}
