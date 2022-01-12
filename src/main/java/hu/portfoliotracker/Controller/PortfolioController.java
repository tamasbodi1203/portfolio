package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Service.PortfolioService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public String listOfPositions(Model model) {

        //Spot
        val spotPortfolioDto = portfolioService.getPortfolioDto(TRADING_TYPE.SPOT);
        model.addAttribute("spotPortfolioDto", spotPortfolioDto);

        // Cross margin
        val crossPortfolioDto = portfolioService.getPortfolioDto(TRADING_TYPE.CROSS);
        model.addAttribute("crossPortfolioDto", crossPortfolioDto);

        // Isolated margin
        val isolatedPortfolioDto = portfolioService.getPortfolioDto(TRADING_TYPE.ISOLATED);
        model.addAttribute("isolatedPortfolioDto", isolatedPortfolioDto);

        model.addAttribute("currency", "$");
        return "home";
    }

}
