package lmc.web;

import lmc.security.CustomUserDetails;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.user.model.User;
import lmc.user.service.UserService;
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

    public UnitController(UserService userService, UnitService unitService) {
        this.userService = userService;
        this.unitService = unitService;
    }

    @GetMapping
    public ModelAndView getUnits(@AuthenticationPrincipal CustomUserDetails details) {
        User user = userService.getUserById(details.getId());
        List<Unit> baseUnits = unitService.getAllActiveUnits();
        ModelAndView modelAndView = new ModelAndView("units");
        modelAndView.addObject("user", user);
        modelAndView.addObject("baseUnits", baseUnits);

        return modelAndView;
    }
}
