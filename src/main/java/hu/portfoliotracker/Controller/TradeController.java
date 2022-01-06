package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Service.CoinMarketCapService;
import hu.portfoliotracker.Service.CsvService;
import hu.portfoliotracker.Service.TradeService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

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

    @GetMapping("/add")
    public String showTradeCreateForm(Trade trade) {
        return "trade-create";
    }

    @PostMapping("/add")
    public String createTrade(@Valid Trade trade, BindingResult result) {
        if (result.hasErrors()) {
            return "trade-create";
        }
        tradeService.saveTrade(trade);
        return "redirect:/trade-history";
    }


    @GetMapping("/delete/{id}")
    public String deleteTrade(@PathVariable long id){
        tradeService.deleteTrade(id);
        return "redirect:/trade-history";
    }


    @GetMapping("/edit/{id}")
    public String editTradeForm(@PathVariable long id, Model model){
        Trade trade = tradeService.getTradeById(id);
        model.addAttribute("trade", trade);
        return "trade-create";
    }
    
    @PostMapping("/edit/{id}")
    public String editTrade(@Valid Trade trade, BindingResult result){
        if (result.hasErrors()) {
            return "trade-create";
        }
        tradeService.saveTrade(trade);
        return "redirect:/trade-history";
    }

    @GetMapping("/import")
    public String importTradesFromCSV() {
        return "trade-import";
    }


    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=import")
    public String importFile(@RequestParam("file") MultipartFile file) {
        csvService.save(file);
        return "redirect:/trade-history";
    }

    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=cancel")
    public String cancelImport() {
        return "redirect:/trade-history";
    }
}
