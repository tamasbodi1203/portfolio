package hu.portfoliotracker.Service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.market.TickerStatistics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.Cryptocurrency;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.CryptocurrencyRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    public void initBaseAssets() {
        RestTemplate restTemplate = new RestTemplate();
        String binanceExchangeInfo = "https://api.binance.com/api/v3/exchangeInfo";
        ResponseEntity<String> response = restTemplate.getForEntity(binanceExchangeInfo, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode symbols = root.path("symbols");
        HashSet<Cryptocurrency> cryptocurrencies = new HashSet<>();
        HashSet<TradingPair> tradingPairs = new HashSet<>();

        for (JsonNode jsonNode : symbols) {

            TradingPair tradingPair = new TradingPair();
            tradingPair.setSymbol(jsonNode.get("symbol").asText());
            tradingPair.setStatus(jsonNode.get("status").asText());
            tradingPair.setBaseAsset(jsonNode.get("baseAsset").asText());
            tradingPair.setQuoteAsset(jsonNode.get("quoteAsset").asText());
            tradingPairs.add(tradingPair);

            Cryptocurrency cryptocurrency = new Cryptocurrency();
            cryptocurrency.setCurrency(jsonNode.get("baseAsset").asText());
            cryptocurrencies.add(cryptocurrency);
            log.info(tradingPair.toString());
        }
        tradingPairRepository.saveAll(tradingPairs);
        cryptocurrencyRepository.saveAll(cryptocurrencies);
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

}
