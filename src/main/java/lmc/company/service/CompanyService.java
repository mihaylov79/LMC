package lmc.company.service;

import lmc.company.model.Company;
import lmc.company.repository.CompanyRepository;
import lmc.exceptions.CompanyAlreadyExistException;
import lmc.web.dto.CreateCompanyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company createNewCompany(CreateCompanyRequest request){

        Optional<Company>existingCompany = repository.findByCompanyEIK(request.getCompanyEIK());

        if (existingCompany.isPresent()){
            throw new CompanyAlreadyExistException("Компания с ЕИК: %s вече съществува!".formatted(request.getCompanyEIK()));
        }

        Company newCompany = Company.builder()
                .companyName(request.getCompanyName())
                .companyEIK(request.getCompanyEIK())
                .VAT(request.getVAT())
                .country(request.getCountry())
                .town(request.getTown())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .manager(request.getManager())
                .contactPerson(request.getContactPerson())
                .active(true)
                .build();

        return repository.save(newCompany);
    }

    public Company editCompanyDetails(CreateCompanyRequest request, UUID companyId){

    Company company = getCompanyById(companyId);

    company = company.toBuilder()
            .companyName(request.getCompanyName())
            .companyEIK(request.getCompanyEIK())
            .VAT(request.getVAT())
            .country(request.getCountry())
            .town(request.getTown())
            .address(request.getAddress())
            .phone(request.getPhone())
            .email(request.getEmail())
            .manager(request.getManager())
            .contactPerson(request.getContactPerson())
            .build();

    return repository.save(company);

    }

    public Company getCompanyById(UUID companyId){
        return repository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException
                        ("Компания с идентификация [ %s ] не е намерена в базата данни"
                                .formatted(companyId)));
    }

    public Company getCompanyByEIK(String companyEIK){

        return repository.findByCompanyEIK(companyEIK)
                .orElseThrow(() -> new IllegalArgumentException
                        ("Компания с ЕИК [ %s ] не е намерена в базата данни"
                                .formatted(companyEIK)));
    }

    public void changeCompanyActiveStatus(Company company){

        if (company.isActive()){
            company = company.toBuilder()
                    .active(false)
                    .build();
        }else {
            company = company.toBuilder()
                    .active(true)
                    .build();
        }

        repository.save(company);
    }

    public List<Company> getAllCompanies(){
       return repository.findAll(Sort.by(
               Sort.Order.desc("active"),
               Sort.Order.asc("companyName")
       ));
    }
}
