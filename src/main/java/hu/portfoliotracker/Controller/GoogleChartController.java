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
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/google-chart")
public class GoogleChartController {

    @Autowired
    private PortfolioService portfolioService;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @GetMapping
    public String index(Model model) {
        List<CountCourse> pieChartData = new ArrayList<>();
        val cc1 = new CountCourse("Humanities", 123);
        val cc2 = new CountCourse("Sciences", 145);
        val cc3 = new CountCourse("Other", 67);
        pieChartData.add(cc1);
        pieChartData.add(cc2);
        pieChartData.add(cc3);

        model.addAttribute("chartData", getDymaicSpotChartData());
        return "google-chart";
    }

    private List<List<Object>> getChartData() {
        return List.of(
                List.of("Mushrooms", RANDOM.nextInt(5)),
                List.of("Onions", RANDOM.nextInt(5)),
                List.of("Olives", RANDOM.nextInt(5)),
                List.of("Zucchini", RANDOM.nextInt(5)),
                List.of("Pepperoni", RANDOM.nextInt(5))
        );
    }

    private List<List<Object>> getSpotChartData() {
        return List.of(
                List.of("BNB", 1189.33),
                List.of("BTC", 1498.72),
                List.of("DOT", 499.83),
                List.of("ETH", 1000.33),
                List.of("LINK", 499.56),
                List.of("MANA", 249.00),
                List.of("PYR", 249.98)
        );
    }

    private List<List<Object>> getDymaicSpotChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val portfolioDtos = portfolioService.refreshPortfolio();
        val spotDto = portfolioDtos.get(0);
        for (val openPosition : spotDto.getOpenPositionDtos()) {
            listOfLists.add(List.of(openPosition.getSymbol(), openPosition.getDeposit()));

        }

        return listOfLists;
    }
}
