package lmc.web;

import jakarta.validation.Valid;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.service.UserService;
import lmc.utils.PasswordGenerator;
import lmc.web.dto.NewUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping ("/users")
public class UserController {

    private final UserService userService;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public UserController(UserService userService, PasswordGenerator passwordGenerator) {
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
    }

    @GetMapping("/create/new")
    public ModelAndView getNewUserForm( @AuthenticationPrincipal CustomUserDetails details) {
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("new-user");
        modelAndView.addObject("user", user);
        modelAndView.addObject("newUserRequest", new NewUserRequest());

        return modelAndView;
    }

    @PostMapping("create/new")
    public ModelAndView createNewUser(@Valid NewUserRequest request, @AuthenticationPrincipal CustomUserDetails details, BindingResult result, RedirectAttributes redirectAttributes){
        User user = userService.getUserById(details.getId());

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("new-user");
            modelAndView.addObject("user", user);
            return  modelAndView;
        }

        String password =  passwordGenerator.generate();
        request.setPassword(password);
        userService.addNewUser(request);

        redirectAttributes.addFlashAttribute("password", password);
        redirectAttributes.addFlashAttribute("email", request.getEmail());

        return new ModelAndView("redirect:/users/create/success");

    }

    @GetMapping("/create/success")
    public ModelAndView showCreateUserSuccessPage(@AuthenticationPrincipal CustomUserDetails details, @ModelAttribute("password") String password, @ModelAttribute("email") String email){
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("create-success");
        modelAndView.addObject("user", user);
        modelAndView.addObject("password", password);
        modelAndView.addObject("email", email);
        return modelAndView;
    }
}
