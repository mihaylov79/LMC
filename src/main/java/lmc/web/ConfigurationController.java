package lmc.web;

import jakarta.validation.Valid;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configuration.service.ConfigurationService;
import lmc.web.dto.CreateNewConfigurationRequest;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final ConfigurableUnitService configurableUnitService;

    public ConfigurationController(ConfigurationService configurationService, ConfigurableUnitService configurableUnitService) {
        this.configurationService = configurationService;
        this.configurableUnitService = configurableUnitService;
    }


    @GetMapping("/create/new")
    public ModelAndView showCreateConfigurationForm(){
        ModelAndView modelAndView = new ModelAndView("new-configuration");
        modelAndView.addObject("configuration", new CreateNewConfigurationRequest());
        modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
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
        return new ModelAndView("redirect:/configurations");
    }



}
