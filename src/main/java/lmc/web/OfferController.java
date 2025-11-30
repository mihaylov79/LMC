package lmc.web;

import jakarta.validation.Valid;
import lmc.company.model.Company;
import lmc.company.service.CompanyService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.offer.model.Offer;
import lmc.offer.service.OfferService;
import lmc.security.CustomUserDetails;
import lmc.unit.model.CurrencyType;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.ConfigurationSnapshotDTO;
import lmc.web.dto.NewOfferRequest;
import lmc.currencyFixing.service.CurrencyConversionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Controller
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;
    private final CompanyService companyService;
    private final ConfigurationService configurationService;
    private final UserService userService;
    private final CurrencyConversionService currencyConversionService;

    public OfferController(OfferService offerService, CompanyService companyService, ConfigurationService configurationService, UserService userService,
                           CurrencyConversionService currencyConversionService) {
        this.offerService = offerService;
        this.companyService = companyService;
        this.configurationService = configurationService;
        this.userService = userService;
        this.currencyConversionService = currencyConversionService;
    }

    @GetMapping("/details/{offerId}")
    public ModelAndView showOfferDetails(@PathVariable UUID offerId, @AuthenticationPrincipal CustomUserDetails details){
        Offer offer = offerService.getOfferWithConfiguration(offerId);
        User user = userService.getUserById(details.getId());

        // Извличаме snapshot-а на конфигурацията с конвертирани цени според валутата на офертата
        ConfigurationSnapshotDTO configurationSnapshot = offerService.getConfigurationSnapshotInDisplayCurrency(offer);

        // Малки, целеви допълнения: изчисляваме display стойности за полетата, които са съхранени в Offer
        BigDecimal displayFinalPrice = offerService.getOfferDisplayFinalPriceUsingSnapshot(offer);
        BigDecimal displayInstallationFee = currencyConversionService.convertWithExchangeRate(offer.getInstallationFee(), offer.getExchangeRate());
        BigDecimal displayDeliveryFee = currencyConversionService.convertWithExchangeRate(offer.getDeliveryFee(), offer.getExchangeRate());
        BigDecimal displayInstallationMaterials = currencyConversionService.convertWithExchangeRate(offer.getInstallationMaterials(), offer.getExchangeRate());

        ModelAndView modelAndView = new ModelAndView("offer-details");
        modelAndView.addObject("user", user);
        modelAndView.addObject("offer", offer);
        modelAndView.addObject("configurationSnapshot", configurationSnapshot);
        modelAndView.addObject("displayFinalPrice", displayFinalPrice);
        modelAndView.addObject("displayInstallationFee", displayInstallationFee);
        modelAndView.addObject("displayDeliveryFee", displayDeliveryFee);
        modelAndView.addObject("displayInstallationMaterials", displayInstallationMaterials);

        return modelAndView;

    }

    @PostMapping("/{offerId}/cancel")
    public ModelAndView cancelOffer(@PathVariable UUID offerId) {
        offerService.cancelOffer(offerId, null);
        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/{offerId}/delete")
    public ModelAndView deleteOffer(@PathVariable UUID offerId) {
        offerService.deleteOffer(offerId);
        return new ModelAndView("redirect:/home");
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

    /**
     * Сменя валутата на офертата за визуализация.
     * Записва exchangeRate към момента на смяната.
     *
     * @param offerId ID на офертата
     * @param currency целева валута (EUR, USD, GBP)
     * @return redirect към offer-details
     */
    @PostMapping("/{offerId}/set-currency")
    public ModelAndView setOfferCurrency(@PathVariable UUID offerId,
                                        @RequestParam("currency") CurrencyType currency) {
        offerService.setOfferCurrency(offerId, currency);
        return new ModelAndView("redirect:/offers/details/" + offerId);
    }
}
