package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/trade-history")
public class TradeController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeController.class);

    private TradeService tradeService;

    @Autowired
    public void setTradeService(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public String listOfTrades(Model model) {
        model.addAttribute("trades", tradeService.getTrades());
        model.addAttribute("currency", "$");
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
}
