package hu.portfoliotracker.Service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.market.TickerStatistics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.portfoliotracker.Model.Cryptocurrency;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.CryptocurrencyRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class BinanceService {

    @Autowired
    TradingPairRepository tradingPairRepository;
    @Autowired
    CryptocurrencyRepository cryptocurrencyRepository;

    BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance("Acp6nGjh0OKrJDhaPxcQN0AIlEnjiIweIQ7ugXNOEh5pyldpwf3YQmM8LRRqrzeP", "A7YmeLWN5NvYqZGUc3CBZ4WGBTun67hZtxBrXrbGbbFLBPy7k6DAdKbFYuzfF0cR");
    BinanceApiRestClient client = factory.newRestClient();

    public double getLastPrice(String tradingPair) {
        TickerStatistics tickerStatistics = client.get24HrPriceStatistics(tradingPair);
        log.info(tradingPair + " latest price: " + tickerStatistics.getLastPrice());
        return Double.parseDouble(tickerStatistics.getLastPrice());
    }


    public void getMyTradesTest() {
        Account account = client.getAccount();
        account.getBalances();
        List<Trade> myTrades = client.getMyTrades("BNBUSDT");
        for (Trade t: myTrades) {
            log.info(t.toString());
        }

    }

    @SneakyThrows
    public JsonNode getAllSymbolData() {
        RestTemplate restTemplate = new RestTemplate();
        String binanceExchangeInfo = "https://api.binance.com/api/v3/exchangeInfo";
        ResponseEntity<String> response = restTemplate.getForEntity(binanceExchangeInfo, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode symbols = root.path("symbols");

        return symbols;
    }

    @SneakyThrows
    public void getAllBaseAssets() {
        RestTemplate restTemplate = new RestTemplate();
        String binanceExchangeInfo = "https://api.binance.com/api/v3/exchangeInfo";
        ResponseEntity<String> response = restTemplate.getForEntity(binanceExchangeInfo, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode symbols = root.path("symbols");
        log.info("valami");
    }

    @SneakyThrows
    public double getLastPriceByDate(String tradingPair, Long seconds) {
        RestTemplate restTemplate = new RestTemplate();
        String binanceExchangeInfo = "https://api.binance.com/api/v3/klines?symbol=" + tradingPair + "&interval=1d&endTime=" + seconds.toString() + "&limit=1";
        ResponseEntity<String> response = restTemplate.getForEntity(binanceExchangeInfo, String.class);
        val mapper = new ObjectMapper();
        val root = mapper.readTree(response.getBody());
        val closePrice = root.get(0).get(4).asDouble();
        log.info(tradingPair + " close price: " + closePrice);
        return closePrice;

    }

}
