package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Model.User;
import hu.portfoliotracker.Service.UserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
@Slf4j
public class AuthController {

    private UserDetailsService userDetailsService;

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, User user, @RequestParam(required = false) String error) {
        if (error != null) {
            // TODO: Hibaüzenet megjelenítése
            model.addAttribute("loginError", true);
            model.addAttribute("user", user);
            log.error("Hibás felhasználónév  és/vagy jelszó");
        }

        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(User user) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }

        userDetailsService.registerUser(user);

        return "redirect:/login";
    }

    @GetMapping(value = "/username")
    @ResponseBody
    public String currentUserName(Authentication authentication) {
        return authentication.getName();
    }

}