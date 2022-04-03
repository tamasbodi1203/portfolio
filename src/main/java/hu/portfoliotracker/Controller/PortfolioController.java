package hu.portfoliotracker.Controller;

import hu.portfoliotracker.DTO.CountCourse;
import hu.portfoliotracker.Service.PortfolioService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping({"/", "/index"})
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    public String listOfPositions(Model model) {


        val portfolioDtos = portfolioService.refreshPortfolio();
        model.addAttribute("spotBalanceDto", portfolioDtos.get(0));
        model.addAttribute("crossBalanceDto", portfolioDtos.get(1));
        model.addAttribute("isolatedBalanceDto", portfolioDtos.get(2));
        model.addAttribute("currency", "$");

        List<CountCourse> pieChartData = new ArrayList<>();
        val cc1 = new CountCourse("Humanities", 123);
        val cc2 = new CountCourse("Sciences", 145);
        val cc3 = new CountCourse("Other", 67);
        pieChartData.add(cc1);
        pieChartData.add(cc2);
        pieChartData.add(cc3);
        model.addAttribute("pieChartData", pieChartData);
        return "home";
    }

    @RequestMapping("content1")
    public String getContent1() {
        return "balance :: content1";
    }

}