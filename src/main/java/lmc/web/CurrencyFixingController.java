package lmc.web;

import jakarta.validation.Valid;
import lmc.currencyFixing.model.CurrencyFixing;
import lmc.currencyFixing.service.CurrencyFixingService;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CurrencyFixingRequest;
import lmc.web.dto.mapper.CustomMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/currency-fixings")
public class CurrencyFixingController {

    private final CurrencyFixingService currencyFixingService;
    private final UserService userService;
    private final CustomMapper customMapper;

    public CurrencyFixingController(CurrencyFixingService currencyFixingService, UserService userService, CustomMapper customMapper) {
        this.currencyFixingService = currencyFixingService;
        this.userService = userService;
        this.customMapper = customMapper;
    }

    @GetMapping
    public ModelAndView showFixingPage(@AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        List<CurrencyFixing> fixings = currencyFixingService.getAllFixings();

        ModelAndView modelAndView = new ModelAndView("currency-fixings");
        modelAndView.addObject("fixings",fixings);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/edit/{fixingId}")
    public ModelAndView getEditPage(@PathVariable UUID fixingId, @AuthenticationPrincipal CustomUserDetails details){
        CurrencyFixing fixing = currencyFixingService.getFixingById(fixingId);
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("edit-currency-fixing");
        modelAndView.addObject("user", user);
        modelAndView.addObject("fixing", fixing);
        modelAndView.addObject("currencyFixingRequest", customMapper.fromFixing(fixing));

        return modelAndView;
    }

    @PutMapping("/edit/{fixingId}")
    public ModelAndView updateExistingFixing(@PathVariable UUID fixingId,
                                             @Valid CurrencyFixingRequest request,
                                             BindingResult result,
                                             RedirectAttributes redirectAttributes,
                                             @AuthenticationPrincipal CustomUserDetails details) {

        if (result.hasErrors()) {
            User user = userService.getUserById(details.getId());
            CurrencyFixing fixing = currencyFixingService.getFixingById(fixingId);

            ModelAndView modelAndView = new ModelAndView("edit-currency-fixing");
            modelAndView.addObject("user", user);
            modelAndView.addObject("fixing", fixing);
            modelAndView.addObject("currencyFixingRequest", request);
            return modelAndView;
        }

        currencyFixingService.saveOrUpdateFixing(request);
        redirectAttributes.addFlashAttribute("success",
                "Курсът за " + request.getCurrency() + " беше успешно обновен!");

        return new ModelAndView("redirect:/currency-fixings");
    }

}

