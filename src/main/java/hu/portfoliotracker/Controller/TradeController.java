package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/trade")
public class TradeController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeController.class);

    private TradeService tradeService;

    @Autowired
    public void setTradeService(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
    }

    @GetMapping
    public String listOfTrades(Model model) {
        model.addAttribute("trades", tradeService.getTrades());
        return "home";
    }

    @GetMapping("/create")
    public String showTradeCreateForm(Trade trade) {
        return "trade-create";
    }

    @PostMapping("/create")
    public String createTrade(@Valid Trade trade, BindingResult result) {
        if (result.hasErrors()) {
            return "trade-create";
        }
        tradeService.saveTrade(trade);
        return "redirect:/trade";
    }


    @GetMapping("/delete/{id}")
    public String deleteTrade(@PathVariable long id){
        tradeService.deleteTrade(id);
        return "redirect:/trade";
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
        return "redirect:/trade";
    }
}
