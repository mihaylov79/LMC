package lmc.web;

import jakarta.validation.Valid;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.option.service.OptionService;
import lmc.security.CustomUserDetails;
import lmc.unit.service.UnitService;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CreateNewConfigurationRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final ConfigurableUnitService configurableUnitService;
    private final UnitService unitService;
    private final OptionService optionService;
    private final UserService userService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService, ConfigurableUnitService configurableUnitService, UnitService unitService, OptionService optionService, UserService userService) {
        this.configurationService = configurationService;
        this.configurableUnitService = configurableUnitService;
        this.unitService = unitService;
        this.optionService = optionService;
        this.userService = userService;
    }


    @GetMapping("/create/new")
    public ModelAndView showCreateConfigurationForm(){
        ModelAndView modelAndView = new ModelAndView("new-configuration");
        modelAndView.addObject("configuration", new CreateNewConfigurationRequest());
        modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
        modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
        modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
        return modelAndView;


    }

    @PostMapping("/create/new")
    public ModelAndView addNewConfiguration(@Valid CreateNewConfigurationRequest request,
                                            BindingResult result) {

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("new-configuration");
            modelAndView.addObject("configuration", request);
            modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
        }
        configurationService.createNewConfiguration(request);
        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/{configurationId}/details")
    public ModelAndView showConfigurationDetails (@PathVariable UUID configurationId, @AuthenticationPrincipal CustomUserDetails details ){
        Configuration configuration = configurationService.findConfigurationById(configurationId);
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("configuration-details");
        modelAndView.addObject("configuration", configuration);
        modelAndView.addObject("user", user);
        return modelAndView;
    }



}
