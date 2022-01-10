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
    private PortfolioService positionService;

    @GetMapping
    public String listOfPositions(Model model) {

        // Töröljük minden futtatáskor a már meglévő pozíciókat, hogy ne duplikálódjanak
        positionService.deleteAll();

        //Spot
        positionService.initPositions(TRADING_TYPE.SPOT);
        val spotPortfolioDto = positionService.getPortfolioDto(TRADING_TYPE.SPOT);
        model.addAttribute("spotPortfolioDto", spotPortfolioDto);

        // Cross margin
        positionService.initPositions(TRADING_TYPE.CROSS);
        val crossPortfolioDto = positionService.getPortfolioDto(TRADING_TYPE.CROSS);
        model.addAttribute("crossPortfolioDto", crossPortfolioDto);

        // Isolated margin
        positionService.initPositions(TRADING_TYPE.ISOLATED);
        val isolatedPortfolioDto = positionService.getPortfolioDto(TRADING_TYPE.ISOLATED);
        model.addAttribute("isolatedPortfolioDto", isolatedPortfolioDto);

        model.addAttribute("currency", "$");
        return "home";
    }
}
