package lmc.web;

import jakarta.validation.Valid;
import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CreateCompanyRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final UserService userService;

    private final CompanyService companyService;

    public CompanyController(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping
    public ModelAndView getAllCompaniesList(@AuthenticationPrincipal CustomUserDetails details){

        User user = userService.getUserById(details.getId());

        List<Company> companies = companyService.getAllCompanies();

        ModelAndView modelAndView = new ModelAndView("companies-list");
        modelAndView.addObject("user", user);
        modelAndView.addObject("companies", companies);

        return modelAndView;

    }

    @PutMapping("/change-starus/{companyId}")
    public String changeStatus(@PathVariable UUID companyId){
        Company company = companyService.getCompanyById(companyId);
        companyService.changeCompanyActiveStatus(company);

        return "redirect:/companies";
    }

    @GetMapping("/create/new")
    public ModelAndView showCreateNewCompanyPage(){
        ModelAndView modelAndView = new ModelAndView("create-company");
        modelAndView.addObject("createCompanyRequest", new CreateCompanyRequest());

        return modelAndView;
    }

    @PostMapping("/create/new")
    public ModelAndView createCompany(@Valid CreateCompanyRequest request, BindingResult result){

        if (result.hasErrors()){
            return new ModelAndView("create-company");

        }

        companyService.createNewCompany(request);

        return new ModelAndView("rdirect:/company");
    }

















































































































































}
