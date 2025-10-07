package lmc.company.model;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
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

}
