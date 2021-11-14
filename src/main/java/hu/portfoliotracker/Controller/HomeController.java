package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private PositionService positionService;

    @GetMapping
    public String listOfPositions(Model model) {
        model.addAttribute("positions", positionService.getPositions());
        return "home";
    }
}
