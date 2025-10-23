package lmc.web;

import jakarta.servlet.http.HttpServletRequest;
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

import lmc.web.dto.mapper.ConfigurationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final ConfigurableUnitService configurableUnitService;
    private final UnitService unitService;
    private final OptionService optionService;
    private final UserService userService;
    private final ConfigurationMapper configurationMapper;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService, ConfigurableUnitService configurableUnitService, UnitService unitService, OptionService optionService, UserService userService, ConfigurationMapper configurationMapper) {
        this.configurationService = configurationService;
        this.configurableUnitService = configurableUnitService;
        this.unitService = unitService;
        this.optionService = optionService;
        this.userService = userService;
        this.configurationMapper = configurationMapper;
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
    public ModelAndView showConfigurationDetails (@PathVariable UUID configurationId, @AuthenticationPrincipal CustomUserDetails details, HttpServletRequest request ){
        Configuration configuration = configurationService.findConfigurationById(configurationId);
        User user = userService.getUserById(details.getId());

        configurationService.updateConfigurationPrice(configurationId);

        ModelAndView modelAndView = new ModelAndView("configuration-details");
        modelAndView.addObject("configuration", configurationMapper.toDto(configuration));
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUrl", request.getRequestURI());
        return modelAndView;
    }

    @GetMapping("/edit/{configurationId}")
    public ModelAndView showEditConfigurationForm(@PathVariable UUID configurationId, @AuthenticationPrincipal CustomUserDetails details){
        Configuration configuration = configurationService.findConfigurationById(configurationId);
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("edit-configuration");
        modelAndView.addObject("configuration", configurationMapper.toEditRequest(configuration));
        modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
        modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
        modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/edit/{configurationId}")
    public ModelAndView editConfigurationData(@PathVariable UUID configurationId, @Valid CreateNewConfigurationRequest request, BindingResult result){
        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("edit-configuration");
            modelAndView.addObject("configuration", request);
            modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
            modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
            modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
            modelAndView.addObject("configurationId", configurationId);
            return modelAndView;
        }
        configurationService.updateConfiguration(configurationId, request);
        return new ModelAndView("redirect:/configurations/{configurationId}/details");
    }

    @PostMapping("/{configurationId}/units/add")
    @ResponseBody
    public Map<String, Object> addNewConfigurationUnit(@PathVariable UUID configurationId,@RequestBody Map<String,Object> body){
        UUID cuId = UUID.fromString((String) body.get("configurableUnitId"));
        int qty = ((Number) body.getOrDefault("quantity",1)).intValue();
        Configuration updated = configurationService.addConfigurableUnit(configurationId,cuId,qty);

        return Map.of("totalPrice", updated.getTotalPrice(),
                      "priceUpdateDate", updated.getPriceUpdateDate());
    }
    @PostMapping("/{configurationId}/units/remove")
    @ResponseBody
    public Map<String, Object> removeConfigurationUnit(@PathVariable UUID configurationId,@RequestBody Map<String,Object> body){
        UUID cuId = UUID.fromString((String) body.get("configurableUnitId"));
        int qty = ((Number) body.getOrDefault("quantity",1)).intValue();

        Configuration updated = configurationService.removeConfigurableUnit(configurationId,cuId,qty);

        return Map.of("totalPrice", updated.getTotalPrice(),
                      "priceUpdateDate", updated.getPriceUpdateDate());
    }



}
