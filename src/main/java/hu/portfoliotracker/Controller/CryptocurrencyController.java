package hu.portfoliotracker.Controller;

import hu.portfoliotracker.Service.BinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/cryptocurrency")
public class CryptocurrencyController {

    @Autowired
    BinanceService binanceService;

    @GetMapping("/init")
    public void initCryptocurrencies() {
        long startTime = System.nanoTime();
        binanceService.initBaseAssets();
        long stopTime = System.nanoTime();
        long elpasedTime = stopTime - startTime;
        //ArrayList, darabonként mentéssel: 78s
        //ArrayList, saveAll : 42s
        //HashSet, saveAll: 13s
        //Párosok és kriptovaluták egyszerre: 51s
        log.info("Cryptocurrency init run time: " + String.valueOf(elpasedTime / 1000000000) + " seconds");

    }
}
