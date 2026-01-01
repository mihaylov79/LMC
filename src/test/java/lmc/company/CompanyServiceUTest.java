package lmc.company;

import lmc.company.model.Company;
import lmc.company.repository.CompanyRepository;
import lmc.company.service.CompanyService;
import lmc.exceptions.CompanyAlreadyExistException;
import lmc.web.dto.CreateCompanyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceUTest {

    @Mock
    private CompanyRepository companyRepository;
    @InjectMocks
    private CompanyService companyService;

    @Test
    void given_ExistingCompany_should_Throw_CompanyAlreadyExistException(){


        Company existing = getExistingCompany();

        CreateCompanyRequest createCompanyDTO = CreateCompanyRequest.builder()
                .companyName("TestCompany")
                .companyEIK("1234567890")
                .VAT("BG1234567890")
                .country("Bulgaria")
                .town("Plovdiv")
                .address("Test str")
                .manager("Test Manager")
                .build();

        when(companyRepository.findByCompanyEIK(createCompanyDTO.getCompanyEIK())).thenReturn(Optional.of(existing));
        assertThrows(CompanyAlreadyExistException.class, () -> companyService.createNewCompany(createCompanyDTO));
        verify(companyRepository, never()).save(any(Company.class));
    }


    @Test
    void given_NoExistingCompany_ShouldCreateNewCompany(){

        CreateCompanyRequest createCompanyDTO = CreateCompanyRequest.builder()
                .companyName("TestCompany")
                .companyEIK("1234567890")
                .VAT("BG1234567890")
                .country("Bulgaria")
                .town("Plovdiv")
                .address("Test str")
                .manager("Test Manager")
                .contactPerson("Contact Person")
                .build();

        when(companyRepository.findByCompanyEIK(createCompanyDTO.getCompanyEIK())).thenReturn(Optional.empty());

        when(companyRepository.save(any(Company.class)))
                .thenAnswer(saveResult -> saveResult.getArgument(0));

        Company result = companyService.createNewCompany(createCompanyDTO);


        assertEquals("TestCompany" , result.getCompanyName());
        assertEquals("1234567890", result.getCompanyEIK());
        assertEquals("BG1234567890", result.getVAT());
        assertEquals("Bulgaria", result.getCountry());
        assertEquals("Plovdiv", result.getTown());
        assertEquals("Test str", result.getAddress());
        assertEquals("Test Manager", result.getManager());
        assertEquals("Contact Person",result.getContactPerson());
        assertTrue(result.isActive());
       verify(companyRepository, times(1)).save(any(Company.class));

    }

    @Test
    void given_EditExistingCompany_should_Return_EditedCompany(){

        Company existing = getExistingCompany();

        UUID existingId = existing.getId();

        CreateCompanyRequest editedDTO = CreateCompanyRequest.builder()
                .companyName("New TestCompany")
                .companyEIK("0987654321")
                .VAT("BG0987654321")
                .country("Bulgaria")
                .town("Sofia")
                .address("New Test str")
                .manager("New Test Manager")
                .build();

        when(companyRepository.findById(existingId)).thenReturn(Optional.of(existing));
        when(companyRepository.save(any(Company.class))).thenAnswer(saveResult -> saveResult.getArgument(0));

        Company edited = companyService.editCompanyDetails(editedDTO,existingId);

        assertEquals("New TestCompany", edited.getCompanyName());
        assertEquals("0987654321", edited.getCompanyEIK());
        assertEquals("BG0987654321", edited.getVAT());
        assertEquals("Sofia", edited.getTown());
        assertEquals("New Test str" , edited.getAddress());
        assertEquals("New Test Manager", edited.getManager());
        verify(companyRepository,times(1)).save(any(Company.class));
    }

    @Test
    void given_nonExistingCompany_Should_Throw_IllegalArgumentException_when_use_getCompanyByEIK(){

        String EIK = "1234567980";

        when(companyRepository.findByCompanyEIK(EIK)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> companyService.getCompanyByEIK(EIK));
    }

    @Test
    void given_nonExistingCompany_Should_Throw_IllegalArgumentException_when_use_getCompanyById(){

        UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> companyService.getCompanyById(companyId));
    }

    private static Company getExistingCompany() {
        return Company.builder()
                .companyName("TestCompany")
                .companyEIK("1234567890")
                .VAT("BG1234567890")
                .country("Bulgaria")
                .town("Plovdiv")
                .address("Test str")
                .manager("Test Manager")
                .build();
    }

}
