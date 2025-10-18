package lmc.web;

import jakarta.validation.Valid;
import lmc.configurableUnit.service.ConfigurableUnitService;
import lmc.option.model.Option;
import lmc.option.service.OptionService;
import lmc.security.CustomUserDetails;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import lmc.web.dto.CreateNewOptionRequest;
import lmc.web.dto.CreateNewUnitRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import lmc.configurableUnit.model.ConfigurableUnit;


import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/products")
public class UnitController {

    private final UserService userService;
    private final UnitService unitService;
    private final OptionService optionService;
    private final ConfigurableUnitService configurableUnitService;

    public UnitController(UserService userService, UnitService unitService, OptionService optionService, ConfigurableUnitService configurableUnitService) {
        this.userService = userService;
        this.unitService = unitService;
        this.optionService = optionService;
        this.configurableUnitService = configurableUnitService;
    }

    @GetMapping
    public ModelAndView getUnits(@AuthenticationPrincipal CustomUserDetails details) {
        User user = userService.getUserById(details.getId());
        List<Unit> baseUnits = unitService.getAllActiveUnits();
        List<Option> unitOptions = optionService.getAllActiveOptions();
        ModelAndView modelAndView = new ModelAndView("units");
        modelAndView.addObject("user", user);
        modelAndView.addObject("baseUnits", baseUnits);
        modelAndView.addObject("unitOptions", unitOptions);

        return modelAndView;
    }

    @GetMapping("/base-units/new")
    public ModelAndView showCreateUnitForm(){
        ModelAndView modelAndView = new ModelAndView("new-base-unit");
        modelAndView.addObject("createNewUnitRequest", new CreateNewUnitRequest());

        return modelAndView;
    }

    @PostMapping("/base-units/new")
    public ModelAndView createNewBaseUnit(@Valid CreateNewUnitRequest request, BindingResult result) {

        if (result.hasErrors()){
            return new ModelAndView("new-base-unit");
        }

        unitService.createNewUnit(request);
        return new ModelAndView("redirect:/products");
    }

    @GetMapping("/unit-options/new")
    public ModelAndView showCreateOptionForm(){
        ModelAndView modelAndView = new ModelAndView("new-option");
        modelAndView.addObject("createNewOptionRequest", new CreateNewOptionRequest());
        return modelAndView;
    }

    @PostMapping("/unit-options/new")
    public ModelAndView createNewOption(@Valid CreateNewOptionRequest request, BindingResult result) {
        if (result.hasErrors()){
            return new ModelAndView("new-option");
        }
        optionService.createNewOption(request);
        return new ModelAndView("redirect:/products");
    }

    @GetMapping("/configurable-units/create")
    public ModelAndView showConfigurableUnitCreateForm() {
        ModelAndView modelAndView = new ModelAndView("new-configurable-unit");
        modelAndView.addObject("request", new CreateNewConfiguredUnitRequest());
        modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
        modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
        return modelAndView;
    }

    @PostMapping("/configurable-units/create")
    public ModelAndView createConfigurableUnit(@Valid CreateNewConfiguredUnitRequest request,
                                               BindingResult result
                                               ) {
        if (result.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-configurable-unit");
            modelAndView.addObject("allUnits", unitService.getAllActiveUnits());
            modelAndView.addObject("allOptions", optionService.getAllActiveOptions());
            return modelAndView;
        }

        configurableUnitService.createUnit(request);
        return new ModelAndView("redirect:/home");
    }

    @PostMapping("/configurable-units/create-new")
    @ResponseBody
    public Map<String, Object> createConfigurableUnitAjax(@RequestBody CreateNewConfiguredUnitRequest request) {
        ConfigurableUnit cu = configurableUnitService.createUnit(request);

        return Map.of(
                "id", cu.getId(),
                "code", cu.getCode(),
                "unit", Map.of(
                        "code", cu.getUnit().getCode(),
                        "name", cu.getUnit().getName()
                )
        );
    }





}
