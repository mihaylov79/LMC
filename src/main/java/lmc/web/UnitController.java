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
import lmc.web.dto.mapper.CustomMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import lmc.configurableUnit.model.ConfigurableUnit;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class UnitController {

    private final UserService userService;
    private final UnitService unitService;
    private final OptionService optionService;
    private final ConfigurableUnitService configurableUnitService;
    private final CustomMapper customMapper;

    public UnitController(UserService userService, UnitService unitService, OptionService optionService, ConfigurableUnitService configurableUnitService, CustomMapper customMapper) {
        this.userService = userService;
        this.unitService = unitService;
        this.optionService = optionService;
        this.configurableUnitService = configurableUnitService;
        this.customMapper = customMapper;
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

    @GetMapping("/base-units/edit/{unitId}")
    public ModelAndView getEditUnitForm(@PathVariable UUID unitId){

        Unit unit = unitService.getUnitById(unitId);

        ModelAndView modelAndView = new ModelAndView("edit-unit");
        modelAndView.addObject("unitId", unitId);
        modelAndView.addObject("createNewUnitRequest", customMapper.fromUnit(unit));

        return modelAndView;

    }

    @PutMapping("/base-units/edit/{unitId}")
    public ModelAndView editExistingUnit(@PathVariable UUID unitId,@Valid CreateNewUnitRequest request, BindingResult result){

        if (result.hasErrors()){
            return new ModelAndView("edit-unit");
        }

        unitService.editUnit(unitId, request);
        ModelAndView modelAndView = new ModelAndView("redirect:/products");
//        modelAndView.addObject("unitId", unitId);
        return modelAndView;

    }

    @GetMapping("/base-units/details/{unitId}")
    public ModelAndView getBaseUnitDetails(@PathVariable UUID unitId, @AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());
        Unit unit = unitService.getUnitById(unitId);

        ModelAndView modelAndView = new ModelAndView("unit-details");
        modelAndView.addObject("user", user);
        modelAndView.addObject("unit", unit);
        return modelAndView;
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
