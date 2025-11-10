package lmc.company.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "company_name")
    private String companyName;

    @Column(nullable = false, unique = true, name = "EIK")
    private String companyEIK;

    @Column
    private String VAT;

    @Column
    private String country;

    @Column
    private String town;

    @Column
    private String address;

    @Column
    private String manager;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private boolean active;

}
