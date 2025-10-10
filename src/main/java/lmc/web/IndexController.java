package lmc.web;

import lmc.web.dto.LoginRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {


    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null){
            modelAndView.addObject("errorMessage", "Грешно потребителско име или парола");
        }

        return modelAndView;
    }
}
