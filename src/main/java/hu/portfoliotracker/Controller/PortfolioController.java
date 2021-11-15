package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioController {

    @Autowired
    private PortfolioService positionService;

    @GetMapping
    public String listOfPositions(Model model) {
        positionService.initPositions();
        model.addAttribute("openPositions", positionService.getOpenPositions());
        model.addAttribute("closedPositions", positionService.getClosedPositions());
        model.addAttribute("currency", "$");
        return "home";
    }
}
