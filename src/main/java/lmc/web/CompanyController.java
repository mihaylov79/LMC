package lmc.web;

import jakarta.validation.Valid;
import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CreateCompanyRequest;
import lmc.web.dto.mapper.CustomMapper;
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
    private final CustomMapper customMapper;

    public CompanyController(UserService userService, CompanyService companyService, CustomMapper customMapper) {
        this.userService = userService;
        this.companyService = companyService;
        this.customMapper = customMapper;
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

    @PutMapping("/change-status/{companyId}")
    public String changeStatus(@PathVariable UUID companyId){
        Company company = companyService.getCompanyById(companyId);
        companyService.changeCompanyActiveStatus(company);

        return "redirect:/companies";
    }

    @GetMapping("/create/new")
    public ModelAndView showCreateNewCompanyPage(@AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("new-company");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createCompanyRequest", new CreateCompanyRequest());

        return modelAndView;
    }

    @PostMapping("/create/new")
    public ModelAndView createCompany(@AuthenticationPrincipal CustomUserDetails details,
                                      @Valid CreateCompanyRequest request, BindingResult result){

        User user = userService.getUserById(details.getId());

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("new-company");
            modelAndView.addObject("user", user);
            modelAndView.addObject("createCompanyRequest", request);
            return modelAndView;

        }

        companyService.createNewCompany(request);

        return new ModelAndView("redirect:/companies");
    }

    @GetMapping("/edit/{companyId}")
    public ModelAndView showEditCompanyPage(@PathVariable UUID companyId, @AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());
        Company company = companyService.getCompanyById(companyId);

        ModelAndView modelAndView = new ModelAndView("edit-company");
        modelAndView.addObject("user", user);
        modelAndView.addObject("company", company);
        modelAndView.addObject("newCompanyRequest", customMapper.fromCompany(company));
        return modelAndView;
    }

    @PutMapping("/edit/{companyId}")
    public ModelAndView editCompany(@PathVariable UUID companyId,
                                    @AuthenticationPrincipal CustomUserDetails details,
                                    @Valid CreateCompanyRequest request, BindingResult result) {
        User user = userService.getUserById(details.getId());

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("edit-company");
            modelAndView.addObject("user", user);
            modelAndView.addObject("newCompanyRequest", request);
            return modelAndView;
        }
        companyService.editCompanyDetails(request,companyId);

        return new ModelAndView("redirect:/companies");
    }

















































































































































}
