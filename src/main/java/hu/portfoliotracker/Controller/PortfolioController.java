package hu.portfoliotracker.Controller;

import hu.portfoliotracker.DTO.BalanceDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Service.PerformanceService;
import hu.portfoliotracker.Service.PortfolioService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping({"/", "/index"})
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private PerformanceService performanceService;
    private List<BalanceDto> balanceDto;

    @GetMapping
    public String main(Model model) {
        balanceDto = portfolioService.refreshPortfolio();
        return "home";
    }

    @GetMapping("{tab}")
    public String tab(Model model, @PathVariable String tab) {
        //val portfolioDtos = portfolioService.refreshPortfolio();
        model.addAttribute("spotBalanceDto", balanceDto.get(0));
        model.addAttribute("crossBalanceDto", balanceDto.get(1));
        model.addAttribute("isolatedBalanceDto", balanceDto.get(2));
        model.addAttribute("currency", "$");

        model.addAttribute("performanceChartData", getPerformanceChartData());
        // TODO: Üres adattal ne jelenjen meg semmi, switch-es refaktorálás
        model.addAttribute("chartDataSpot", getDymaicSpotChartData());
        model.addAttribute("chartDataCross", getDymaicCrossChartData());
        model.addAttribute("chartDataIsolated", getDymaicIsoaltedChartData());
        return "_" + tab;
    }

    @GetMapping("/performance")
    public String calculatePerformance(Model model){

        model.addAttribute("chartData", getSpotPerformanceChartData());
        return "performance-chart";
    }

    @RequestMapping("content1")
    public String getContent1() {
        return "balance :: content1";
    }

    private List<List<Object>> getDymaicSpotChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val spotDto = balanceDto.get(0);
        for (val openPosition : spotDto.getOpenPositionDtos()) {
            listOfLists.add(List.of(openPosition.getSymbol(), openPosition.getMarketValue()));
        }

        return listOfLists;
    }

    private List<List<Object>> getDymaicCrossChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val crossDto = balanceDto.get(1);
        for (val openPosition : crossDto.getOpenPositionDtos()) {
            listOfLists.add(List.of(openPosition.getSymbol(), openPosition.getMarketValue()));
        }

        return listOfLists;
    }

    private List<List<Object>> getDymaicIsoaltedChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val isolatedDto = balanceDto.get(2);
        for (val openPosition : isolatedDto.getOpenPositionDtos()) {
            listOfLists.add(List.of(openPosition.getSymbol(), openPosition.getMarketValue()));
        }

        return listOfLists;
    }

    private List<List<Object>> getSpotPerformanceChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val snapshots = performanceService.calculatePerformance(TRADING_TYPE.SPOT);
        for (val snapshot : snapshots){
            listOfLists.add(List.of(snapshot.getDate().toString(), snapshot.getTotalValue()));
        }

        return listOfLists;
    }

    private List<List<Object>> getPerformanceChartData() {
        val listOfLists = new ArrayList<List<Object>>();
        val snapshots = performanceService.getLastSevenDays();
        for (val snapshot : snapshots){
            listOfLists.add(List.of(snapshot.getDate().toString(), snapshot.getAccountTotal()));
        }

        return listOfLists;
    }
}