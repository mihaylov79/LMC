package lmc.web;

import jakarta.validation.Valid;
import lmc.security.CustomUserDetails;
import lmc.user.model.User;
import lmc.user.model.UserRole;
import lmc.user.service.UserService;
import lmc.utils.PasswordGenerator;
import lmc.web.dto.NewPasswordRequest;
import lmc.web.dto.NewUserRequest;
import lmc.web.dto.UpdateUserDetailsRequest;
import lmc.web.dto.mapper.CustomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;


@Controller
@RequestMapping ("/users")
public class UserController {

    private final UserService userService;
    private final PasswordGenerator passwordGenerator;

    private final CustomMapper customMapper;

    @Autowired
    public UserController(UserService userService, PasswordGenerator passwordGenerator, CustomMapper customMapper) {
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
        this.customMapper = customMapper;
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
    public ModelAndView createNewUser(@Valid NewUserRequest request, BindingResult result,
                                      @AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("new-user");
            modelAndView.addObject("user", user);
            return  modelAndView;
        }

        String password =  passwordGenerator.generate();
        request.setPassword(password);
        userService.addNewUser(request);


        ModelAndView modelAndView = new ModelAndView("create-success");
        modelAndView.addObject("user", user);
        modelAndView.addObject("password", password);
        modelAndView.addObject("email", request.getEmail());

        return modelAndView;

    }


    @GetMapping("/change-password")
    public ModelAndView getPasswordChangePage(@AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("password-reset");
        modelAndView.addObject("user", user);
        modelAndView.addObject("newPasswordRequest", new NewPasswordRequest());

        return modelAndView;
    }

    @PostMapping("/change-password")
    public ModelAndView passwordChange(@Valid NewPasswordRequest request,
                                       BindingResult result,
                                       @AuthenticationPrincipal CustomUserDetails details){

        User user = userService.getUserById(details.getId());

        if (result.hasErrors()){
            ModelAndView modelAndView = new ModelAndView("password-reset");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        userService.changePassword(request);

        return new ModelAndView("redirect:/home");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-status/{userId}")
    public String changeStatus(@PathVariable UUID userId){

        userService.changeUserStatus(userId);

        return "redirect:/users/list";
    }

    @GetMapping("/list")
    public ModelAndView getActiveUsers(@AuthenticationPrincipal CustomUserDetails details) {
        User user = userService.getUserById(details.getId());

        ModelAndView modelAndView = new ModelAndView("users-list");
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", userService.getaAllWithoutLoggedUser(user));

        return modelAndView;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-role/{userId}")
    public String userRoleUpdate (@PathVariable UUID userId, @RequestParam UserRole role){

        userService.updateUserRole(role, userId);

        return "redirect:/users/list";
    }

    @GetMapping("/edit-details")
    public ModelAndView getEditUserDetailsPage(@AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());
        ModelAndView modelAndView = new ModelAndView("edit-user-details");
        modelAndView.addObject("user", user);
        modelAndView.addObject("updateUserDetailsRequest", customMapper.DetailsRequestFromUser(user));
        return modelAndView;
    }

    @PutMapping("/edit-details")
    public ModelAndView updateActiveUserDetails(@Valid UpdateUserDetailsRequest request, BindingResult result,
                                                @AuthenticationPrincipal CustomUserDetails details){
        User user = userService.getUserById(details.getId());

        if (result.hasErrors()) {

            ModelAndView modelAndView = new ModelAndView("edit-user-details");
            modelAndView.addObject("user", user);
            return  modelAndView;
        }

        userService.editUserDetails(request);

        return new ModelAndView("redirect:/home");


    }
}
