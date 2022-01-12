package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Service.CoinMarketCapService;
import hu.portfoliotracker.Service.CsvService;
import hu.portfoliotracker.Service.PortfolioService;
import hu.portfoliotracker.Service.TradeService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/trade-history")
public class TradeController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;

    @Autowired
    private CsvService csvService;

    @Autowired
    private CoinMarketCapService coinMarketCapService;

    @Autowired
    private PortfolioService portfolioService;

    @GetMapping
    @SneakyThrows
    public String listOfTrades(Model model) {
        model.addAttribute("trades", tradeService.getTrades());
        model.addAttribute("currency", "$");
        //coinMarketCapService.testCoinMarketCapApi();
        return "trade-history";
    }

    @RequestMapping(method=RequestMethod.POST, params="action=add")
    public String addTrade(){
        return "redirect:/trade-history/add";
    }

    @RequestMapping(method=RequestMethod.POST, params="action=import")
    public String importTrades(){
        return "redirect:/trade-history/import";
    }

    @RequestMapping(method=RequestMethod.POST, params="action=clear")
    public String deleteAllTrades(){
        tradeService.deleteAllTrades();
        return "trade-history";
    }

    // Hozzáadás
    @GetMapping("/add")
    public String showTradeCreateForm(Model model, Trade trade) {

        model.addAttribute("tradingPairs", tradeService.getAllTradingPairs());
        return "trade-create";
    }

    @PostMapping("/add")
    public String createTrade(Model model, @Valid Trade trade, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("tradingPairs", tradeService.getAllTradingPairs());
            return "trade-create";
        }
        trade.setDate(LocalDateTime.now()); //TODO: dátum mokkolást kivenni
        tradeService.saveTrade(trade);
        portfolioService.initBalances();
        return "redirect:/trade-history";
    }

    // Törlés
    @GetMapping("/delete/{id}")
    public String deleteTrade(@PathVariable long id){
        tradeService.deleteTrade(id);
        portfolioService.initBalances();
        return "redirect:/trade-history";
    }


    // Módosítás
    @GetMapping("/edit/{id}")
    public String editTradeForm(Model model, @PathVariable long id){
        Trade trade = tradeService.getTradeById(id);
        model.addAttribute("tradingPairs", tradeService.getAllTradingPairs());
        model.addAttribute("trade", trade);
        return "trade-create";
    }
    
    @PostMapping("/edit/{id}")
    public String editTrade(Model model, @Valid Trade trade, BindingResult result){
        if (result.hasErrors()) {
            model.addAttribute("tradingPairs", tradeService.getAllTradingPairs());
            return "trade-create";
        }
        trade.setDate(LocalDateTime.now()); //TODO: dátum mokkolást kivenni
        tradeService.saveTrade(trade);
        portfolioService.initBalances();
        return "redirect:/trade-history";
    }

    // Importálás
    @GetMapping("/import")
    public String importTradesFromCSV() {
        return "trade-import";
    }


    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=import")
    public String importFile(@RequestParam("file") MultipartFile file, @RequestParam ("type") TRADING_TYPE tradingType) {
        csvService.save(file, tradingType);
        portfolioService.initBalances();
        return "redirect:/trade-history";
    }

    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=cancel")
    public String cancelImport() {
        return "redirect:/trade-history";
    }
}
