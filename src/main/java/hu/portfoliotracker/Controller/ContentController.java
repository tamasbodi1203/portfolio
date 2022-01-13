package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Service.PortfolioService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("balance")
public class ContentController {

    @Autowired
    private PortfolioService portfolioService;

    @RequestMapping("")
    public String loadContent() {
        return "website";
    }

    @RequestMapping("content1")
    public String getContent1() {
        return "balance :: content1";
    }

    @RequestMapping("content2")
    public String getContent2(Model model) {
        val portfolioDtos = portfolioService.refreshPortfolio();
        model.addAttribute("spotBalanceDto", portfolioDtos.get(0));
        model.addAttribute("crossBalanceDto", portfolioDtos.get(1));
        model.addAttribute("isolatedBalanceDto", portfolioDtos.get(2));

        model.addAttribute("currency", "$");

        return "balance :: content2";
    }

}