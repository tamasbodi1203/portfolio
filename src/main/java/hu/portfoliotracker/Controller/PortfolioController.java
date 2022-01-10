package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Enum.TRADING_TYPE;
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

        //Spot
        positionService.initPositions(TRADING_TYPE.SPOT);
        model.addAttribute("spotOpenPositions", positionService.getOpenPositions(TRADING_TYPE.SPOT));
        model.addAttribute("spotClosedPositions", positionService.getClosedPositions(TRADING_TYPE.SPOT));

        // Cross margin
        positionService.initPositions(TRADING_TYPE.CROSS);
        model.addAttribute("crossOpenPositions", positionService.getOpenPositions(TRADING_TYPE.CROSS));
        model.addAttribute("crossClosedPositions", positionService.getClosedPositions(TRADING_TYPE.CROSS));

        // Isolated margin
        positionService.initPositions(TRADING_TYPE.ISOLATED);
        model.addAttribute("isolatedOpenPositions", positionService.getOpenPositions(TRADING_TYPE.ISOLATED));
        model.addAttribute("isolatedClosedPositions", positionService.getClosedPositions(TRADING_TYPE.ISOLATED));

        model.addAttribute("currency", "$");
        return "home";
    }
}
