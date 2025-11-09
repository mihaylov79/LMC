package lmc.company.service;

import lmc.company.model.Company;
import lmc.company.repository.CompanyRepository;
import lmc.exceptions.CompanyAlreadyExistException;
import lmc.user.model.User;
import lmc.web.dto.CreateCompanyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyService {

    private CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company createNewCompany(CreateCompanyRequest request){

        Optional<Company>existingUser = repository.findByCompanyEIK(request.getCompanyEIK());

        if (existingUser.isPresent()){
            throw new CompanyAlreadyExistException("Компания с ЕИК: %s вече съществува!".formatted(request.getCompanyEIK()));
        }

        Company newCompany = Company.builder()
                .companyName(request.getCompanyName())
                .companyEIK(request.getCompanyEIK())
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
}
