package hu.portfoliotracker.Service;

import com.fasterxml.jackson.databind.JsonNode;
import hu.portfoliotracker.Model.Cryptocurrency;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.CryptocurrencyRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@Service
public class CryptocurrencyService {

    @Autowired
    TradingPairRepository tradingPairRepository;
    @Autowired
    CryptocurrencyRepository cryptocurrencyRepository;

    @Autowired
    BinanceService binanceService;
    @Autowired
    CoinMarketCapService coinMarketCapService;

    public void initBaseAssets() {
        tradingPairRepository.deleteAll();
        cryptocurrencyRepository.deleteAll();

        JsonNode coinMarketCapData = coinMarketCapService.getAllCryptocurrencyData();

        JsonNode symbols = binanceService.getAllSymbolData();
        HashSet<Cryptocurrency> cryptocurrencies = new HashSet<>();
        HashSet<TradingPair> tradingPairs = new HashSet<>();

        for (JsonNode symbol : symbols) {

            TradingPair tradingPair = new TradingPair();
            tradingPair.setSymbol(symbol.get("symbol").asText());
            tradingPair.setStatus(symbol.get("status").asText());
            tradingPair.setBaseAsset(symbol.get("baseAsset").asText());
            tradingPair.setQuoteAsset(symbol.get("quoteAsset").asText());
            tradingPairs.add(tradingPair);

            Cryptocurrency cryptocurrency = new Cryptocurrency();
            cryptocurrency.setCurrency(symbol.get("baseAsset").asText());
            cryptocurrencies.add(cryptocurrency);
            log.info(tradingPair.toString());
        }

        for (JsonNode data : coinMarketCapData) {
            for (Cryptocurrency c :  cryptocurrencies) {
                if (c.getCurrency().equals(data.get("symbol").asText())) {
                    c.setCmcId(data.get("id").asLong());
                }
            }
        }


        tradingPairRepository.saveAll(tradingPairs);
        cryptocurrencyRepository.saveAll(cryptocurrencies);
    }
}
