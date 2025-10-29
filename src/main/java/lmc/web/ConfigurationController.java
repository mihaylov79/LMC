package lmc.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.configurableUnit.service.ConfiguredUnitService;
import lmc.configuration.model.Configuration;
import lmc.configuration.service.ConfigurationService;
import lmc.option.service.OptionService;
import lmc.security.CustomUserDetails;
import lmc.unit.service.UnitService;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.ConfigurationUnitRequest;
import lmc.web.dto.CreateNewConfigurationRequest;

import lmc.web.dto.OptionSelectionDTO;
import lmc.web.dto.mapper.ConfigurationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
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
    private final ConfiguredUnitService configuredUnitService;

    @Autowired
    public ConfigurationController(ConfigurationService configurationService, ConfigurableUnitService configurableUnitService, UnitService unitService, OptionService optionService, UserService userService, ConfigurationMapper configurationMapper, ConfiguredUnitService configuredUnitService) {
        this.configurationService = configurationService;
        this.configurableUnitService = configurableUnitService;
        this.unitService = unitService;
        this.optionService = optionService;
        this.userService = userService;
        this.configurationMapper = configurationMapper;
        this.configuredUnitService = configuredUnitService;
    }


    @GetMapping("/create/new")
    public ModelAndView showCreateConfigurationForm(){
        ModelAndView modelAndView = new ModelAndView("new-configuration");
        modelAndView.addObject("configuration", new CreateNewConfigurationRequest());
        modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
        modelAndView.addObject("configuredUnits", configuredUnitService.getAllWithOptions()); // <--- added
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
            modelAndView.addObject("configuredUnits", configuredUnitService.getAllWithOptions()); // <--- added
            modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
            modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
            return modelAndView;
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
        modelAndView.addObject("configuredUnits", configuredUnitService.getAllWithOptions()); // <--- added
        modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
        modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
        modelAndView.addObject("user", user);
        modelAndView.addObject("configurationId", configurationId);
        return modelAndView;
    }

    @PostMapping("/edit/{configurationId}")
    public ModelAndView editConfigurationData(@PathVariable UUID configurationId, @Valid CreateNewConfigurationRequest request, BindingResult result){
        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("edit-configuration");
            modelAndView.addObject("configuration", request);
            modelAndView.addObject("existingUnits", configurableUnitService.getAllUnits());
            modelAndView.addObject("configuredUnits", configuredUnitService.getAllWithOptions()); // <--- added
            modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
            modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
            modelAndView.addObject("configurationId", configurationId);
            return modelAndView;
        }
        configurationService.updateConfiguration(configurationId, request);
        return new ModelAndView("redirect:/configurations/edit/" + configurationId);
    }

    @PostMapping("/{configurationId}/units/add")
    @ResponseBody
    public Map<String, Object> addNewConfigurationUnit(@PathVariable UUID configurationId, @RequestBody ConfigurationUnitRequest request){
        UUID cuId = request.getConfigurableUnitId();
        int qty = Math.max(1, request.getQuantity());
        List<OptionSelectionDTO> optionSelections = request.getOptionSelections();

        Configuration updated = configurationService.addConfigurableUnit(configurationId, cuId, qty, optionSelections);

        return Map.of("totalPrice", updated.getTotalPrice(),
                      "priceUpdateDate", updated.getPriceUpdateDate());
    }

    @PostMapping("/{configurationId}/units/remove")
    @ResponseBody
    public Map<String, Object> removeConfigurationUnit(@PathVariable UUID configurationId,
                                                       @RequestBody ConfigurationUnitRequest request) {

        UUID cuId = request.getConfigurableUnitId();
        int qty = Math.max(1, request.getQuantity());
        List<OptionSelectionDTO> optionSelections = request.getOptionSelections();

        Configuration updated = configurationService.removeConfigurableUnit(configurationId, cuId, qty, optionSelections);

        return Map.of("totalPrice", updated.getTotalPrice(),
                "priceUpdateDate", updated.getPriceUpdateDate());
    }

    @GetMapping("/create-from/{configurationId}")
    public ModelAndView createNewConfigurationFromExisting(@PathVariable UUID configurationId){

        Configuration configuration = configurationService.findConfigurationById(configurationId);

        CreateNewConfigurationRequest request = configurationMapper.newFromExisting(configuration);

        Configuration created = configurationService.createNewConfiguration(request);

        return new ModelAndView("redirect:/configurations/edit/" + created.getId());
    }


}
