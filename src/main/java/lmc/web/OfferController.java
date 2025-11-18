package lmc.web;

import jakarta.validation.Valid;
import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.offer.model.Offer;
import lmc.offer.service.OfferService;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.ConfigurationSnapshotDTO;
import lmc.web.dto.NewOfferRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;
    private final CompanyService companyService;
    private final ConfigurationService configurationService;
    private final UserService userService;

    public OfferController(OfferService offerService, CompanyService companyService, ConfigurationService configurationService, UserService userService) {
        this.offerService = offerService;
        this.companyService = companyService;
        this.configurationService = configurationService;
        this.userService = userService;
    }

    @GetMapping("/details/{offerId}")
    public ModelAndView showOfferDetails(@PathVariable UUID offerId){
        Offer offer = offerService.getOfferWithConfiguration(offerId);

        // Извличаме snapshot-а на конфигурацията към момента на създаване на офертата
        ConfigurationSnapshotDTO configurationSnapshot = offerService.getConfigurationSnapshot(offer);

        ModelAndView modelAndView = new ModelAndView("offer-detalis");
        modelAndView.addObject("offer", offer);
        modelAndView.addObject("configurationSnapshot", configurationSnapshot);

        return modelAndView;

    }

    @GetMapping("/create/new")
    public ModelAndView showCreateOfferPage(@AuthenticationPrincipal CustomUserDetails details) {
        User user = userService.getUserById(details.getId());
        List<Company> companies = companyService.getAllCompanies();
        List<Configuration> configurations = configurationService.getAllConfigurations();

        ModelAndView modelAndView = new ModelAndView("new-offer");
        modelAndView.addObject("user", user);
        modelAndView.addObject("companies", companies);
        modelAndView.addObject("configurations", configurations);
        modelAndView.addObject("newOfferRequest", new NewOfferRequest());
        return modelAndView;
    }

    @PostMapping("/create/new")
    public ModelAndView createOffer(@AuthenticationPrincipal CustomUserDetails details,
                                    @Valid @ModelAttribute("newOfferRequest") NewOfferRequest req,
                                    BindingResult result) {
        User user = userService.getUserById(details.getId());

        if (result.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-offer");
            modelAndView.addObject("user", user);
            modelAndView.addObject("companies", companyService.getAllCompanies());
            modelAndView.addObject("configurations", configurationService.getAllConfigurations());
            modelAndView.addObject("newOfferRequest", req);
            return modelAndView;
        }

        offerService.createNewOffer(req, user);
        return new ModelAndView("redirect:/home");
    }
}
