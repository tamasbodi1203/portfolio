package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Service.*;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

@Controller
@RequestMapping("/trade-history")
public class TradeController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeController.class);

    @Autowired
    private TradeService tradeService;
    @Autowired
    private ImportService importService;
    @Autowired
    private CoinMarketCapService coinMarketCapService;
    @Autowired
    private PortfolioService portfolioService;
    @Autowired
    private PerformanceService performanceService;

    @GetMapping
    @SneakyThrows
    public String main(Model model) {
        return "trade-history";
    }

    @GetMapping("{tab}")
    public String tab(Model model, @PathVariable String tab) {
        if (Arrays.asList("tab1", "tab2", "tab3")
                .contains(tab)) {
            if ("tab1".equals(tab)) {
                model.addAttribute("trades", tradeService.getAllByTradingType(TRADING_TYPE.SPOT));
            }
            if ("tab2".equals(tab)) {
                model.addAttribute("trades", tradeService.getAllByTradingType(TRADING_TYPE.CROSS));
            }
            if ("tab3".equals(tab)) {
                model.addAttribute("trades", tradeService.getAllByTradingType(TRADING_TYPE.ISOLATED));
            }
            model.addAttribute("currency", "$");
            return "_" + tab;
        }

        return "empty";
    }

    @RequestMapping(method=RequestMethod.POST, params="action=add")
    public String addTrade(){
        return "redirect:/trade-history/add";
    }

    @RequestMapping(method=RequestMethod.POST, params="action=import")
    public String importTrades(){
        return "redirect:/trade-history/import";
    }

    // Lista törlés
    // TODO: Törlés után a megnyitott fülre térjünk vissza
    @RequestMapping(method=RequestMethod.POST, params="action=clear_spot")
    public String deleteAllSpotTrades(){
        tradeService.deleteAllTradesByTradingType(TRADING_TYPE.SPOT);
        portfolioService.initBalances();
        return "trade-history";
    }
    @RequestMapping(method=RequestMethod.POST, params="action=clear_cross")
    public String deleteAllCrossTrades(){
        tradeService.deleteAllTradesByTradingType(TRADING_TYPE.CROSS);
        portfolioService.initBalances();
        return "trade-history";
    }
    @RequestMapping(method=RequestMethod.POST, params="action=clear_isolated")
    public String deleteAllIsolatedTrades(){
        tradeService.deleteAllTradesByTradingType(TRADING_TYPE.ISOLATED);
        portfolioService.initBalances();
        return "trade-history";
    }
    @RequestMapping(method=RequestMethod.POST, params="action=clear_all")
    public String deleteAllTrades(){
        tradeService.deleteAllTrades();
        portfolioService.initBalances();
        return "trade-history";
    }

    // Szűrés
    @RequestMapping(method=RequestMethod.POST, params="action=filter")
    public String filterTrades(
            Model model, @RequestParam(value = "checkbox_spot", required = false) String[] checkboxSpot,
            @RequestParam(value = "checkbox_cross", required = false) String[] checkboxCross,
            @RequestParam(value = "checkbox_isolated", required = false) String[] checboxIsolated) {
        val trades = new HashSet<Trade>();
        if (checkboxSpot != null) {
            trades.addAll(tradeService.getAllByTradingType(TRADING_TYPE.SPOT));
        }
        if (checkboxCross != null) {
            trades.addAll(tradeService.getAllByTradingType(TRADING_TYPE.CROSS));
        }
        if (checboxIsolated != null) {
            trades.addAll(tradeService.getAllByTradingType(TRADING_TYPE.ISOLATED));
        }
        model.addAttribute("trades", trades);
        model.addAttribute("currency", "$");
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

    @RequestMapping(value = "/add", method=RequestMethod.POST, params="action=cancel")
    public String cancelCreate() {
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

    @RequestMapping(value = "/edit/{id}", method=RequestMethod.POST, params="action=cancel")
    public String cancelEdit() {
        return "redirect:/trade-history";
    }

    // Törlés
    @GetMapping("/delete/{id}")
    public String deleteTrade(@PathVariable long id){
        tradeService.deleteTrade(id);
        portfolioService.initBalances();
        return "redirect:/trade-history";
    }


    // Importálás
    @GetMapping("/import")
    public String importFromFile() {
        return "trade-import";
    }


    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=import")
    public String importFromFile(@RequestParam("file") MultipartFile file, @RequestParam ("type") TRADING_TYPE tradingType) {
        val importIsSuccessful = importService.importFromFile(file, tradingType);
        if (importIsSuccessful) {
            performanceService.deleteAllSnapshotsByUser();
            performanceService.getLastSevenDays();
            portfolioService.initBalances();
            return "redirect:/trade-history";
        } else {
            return "trade-import";
        }
    }

    @RequestMapping(value = "/import", method=RequestMethod.POST, params="action=cancel")
    public String cancelImport() {
        return "redirect:/trade-history";
    }


}