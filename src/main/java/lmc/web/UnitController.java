package lmc.web;

import lmc.option.model.Option;
import lmc.option.service.OptionService;
import lmc.security.CustomUserDetails;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.web.dto.CreateNewUnitRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/products")
public class UnitController {

    private final UserService userService;
    private final UnitService unitService;
    private final OptionService optionService;

    public UnitController(UserService userService, UnitService unitService, OptionService optionService) {
        this.userService = userService;
        this.unitService = unitService;
        this.optionService = optionService;
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

    @GetMapping("/base-unit/new")
    public ModelAndView showCreateUnitForm(){
        ModelAndView modelAndView = new ModelAndView("new-base-unit");
        modelAndView.addObject("createNewUnitRequest", new CreateNewUnitRequest());

        return modelAndView;
    }
}
