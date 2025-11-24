package lmc.web.dto.mapper;

import lmc.company.model.Company;
import lmc.currencyFixing.model.CurrencyFixing;
import lmc.unit.model.Unit;
import lmc.user.model.User;
import lmc.web.dto.CreateCompanyRequest;
import lmc.web.dto.CreateNewUnitRequest;
import lmc.web.dto.CurrencyFixingRequest;
import lmc.web.dto.UpdateUserDetailsRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomMapper {

    public CreateNewUnitRequest fromUnit(Unit unit){

        return CreateNewUnitRequest.builder()
                .imageUrl(unit.getImageUrl())
                .code(unit.getCode())
                .name(unit.getName())
                .description(unit.getDescription())
                .size(unit.getSize())
                .price(unit.getPrice())
                .build();
    }

    public CreateCompanyRequest fromCompany(Company company){

        return CreateCompanyRequest.builder()
                .companyName(company.getCompanyName())
                .companyEIK(company.getCompanyEIK())
                .VAT(company.getVAT())
                .country(company.getCountry())
                .town(company.getTown())
                .address(company.getAddress())
                .manager(company.getManager())
                .contactPerson(company.getContactPerson())
                .phone(company.getPhone())
                .email(company.getEmail())
                .build();
    }

    public UpdateUserDetailsRequest DetailsRequestFromUser(User user){

        return UpdateUserDetailsRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public CurrencyFixingRequest fromFixing(CurrencyFixing fixing){

        return CurrencyFixingRequest.builder()
                .currency(fixing.getCurrency())
                .rate(fixing.getRate())
                .build();
    }

}
