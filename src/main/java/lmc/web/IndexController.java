package lmc.web;

import lmc.configuration.service.ConfigurationService;
import lmc.offer.service.OfferService;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.LoginRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;
    private final ConfigurationService configurationService;
    private final OfferService offerService;

    public IndexController(UserService userService, ConfigurationService configurationService, OfferService offerService) {
        this.userService = userService;
        this.configurationService = configurationService;
        this.offerService = offerService;
    }


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null){
            modelAndView.addObject("errorMessage", "Грешно потребителско име или парола");
        }

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("configurations", configurationService.getAllConfigurations());
        modelAndView.addObject("offers", offerService.getAllOffers());

        return modelAndView;

    }
}
