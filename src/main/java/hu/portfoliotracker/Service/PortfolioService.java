package hu.portfoliotracker.Service;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.ClosedPosition;
import hu.portfoliotracker.Model.OpenPosition;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class PortfolioService {

    @Autowired
    private OpenPositionRepository openPositionRepository;
    @Autowired
    private ClosedPositionRepository closedPositionRepository;
    @Autowired
    private CryptocurrencyRepository cryptocurrencyRepository;
    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    private BinanceService binanceService;
    @Autowired
    TradingPairRepository tradingPairRepository;

    //@Async("threadPoolTaskExecutor")
    public void initPositions(TRADING_TYPE tradingType) {
        System.out.println("Execute method asynchronously. "
                + Thread.currentThread().getName());

        // Töröljük minden futtatáskor a már meglévő pozíciókat, hogy ne duplikálódjanak
        openPositionRepository.deleteAll();
        closedPositionRepository.deleteAll();
        //List<Trade> trades = tradeRepository.findAllByOrderByDate();
        // Kereskedési típus alapján elkülönítjük a kereskedéseket
        List<Trade> trades = tradeRepository.findByTradingTypeOrderByOrderByDate(tradingType);
        Iterator<Trade> iterator = trades.iterator();
        while (iterator.hasNext()) {
            Trade t = iterator.next();
            TradingPair tradingPair = tradingPairRepository.findBySymbol(t.getPair());
            String baseAsset = tradingPair.getBaseAsset();
            // Vétel
            if (t.getSide().equals("BUY")){
                Double currentPrice = binanceService.getLastPrice(baseAsset + "USDT");

                // Megnézzük, hogy létezik-e már nyitott számla az adott kriptovalutával ha nem, akkor nyitunk egyet
                if (openPositionRepository.countBySymbol(baseAsset) == 0L) {
                    Double averageCostBasis = t.getTotal() /t.getAmount();
                    OpenPosition openPosition = OpenPosition.builder()
                            .symbol(tradingPair.getBaseAsset())
                            .cmcId(cryptocurrencyRepository.findByCurrency(tradingPair.getBaseAsset()).getCmcId())
                            .currentPrice(currentPrice)
                            .date(trades.get(0).getDate())
                            .deposit(t.getTotal())
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .marketValue(t.getAmount() * currentPrice)
                            .profit((currentPrice - averageCostBasis) * t.getAmount())
                            .build();

                    saveOpenPosition(openPosition);
                } else {
                    OpenPosition openPosition = openPositionRepository.findBySymbol(baseAsset);
                    Double deposit = openPosition.getDeposit() + t.getTotal();
                    Double quantity = openPosition.getQuantity() + t.getAmount();
                    Double averageCostBasis = deposit / quantity;

                    openPosition.setCurrentPrice(currentPrice);
                    openPosition.setDeposit(deposit);
                    openPosition.setQuantity(quantity);
                    openPosition.setAverageCostBasis(averageCostBasis);
                    openPosition.setMarketValue(quantity * currentPrice);
                    openPosition.setProfit((currentPrice - averageCostBasis) * quantity);
                    saveOpenPosition(openPosition);

                }

                // Eladás
            } else {
                OpenPosition openPosition = openPositionRepository.findBySymbol(baseAsset);
                Double averageCostBasis = openPosition.getAverageCostBasis();

                ClosedPosition closedPosition = ClosedPosition.builder()
                        .symbol(tradingPair.getSymbol())
                        .date(t.getDate())
                        .sellPrice(t.getPrice())
                        .deposit(t.getAmount() * averageCostBasis)
                        .quantity(t.getAmount())
                        .averageCostBasis(averageCostBasis)
                        .marketValue(t.getAmount() * t.getPrice())
                        .profit(t.getAmount() * (t.getPrice() - averageCostBasis))
                        .build();

                saveClosedPosition(closedPosition);

                // Eladás után újra kalkulkuláljuk a megfelelő nyitott pozíciót, ha nem 0 a hátramaradt mennyiség
                if (openPosition.getQuantity() - closedPosition.getQuantity() != 0L) {
                    openPosition.setQuantity(openPosition.getQuantity() - closedPosition.getQuantity());
                    openPosition.setDeposit(openPosition.getDeposit() - closedPosition.getDeposit());
                } else {
                    openPositionRepository.delete(openPosition);
                }
            }
        }
    }

    public void saveOpenPosition(OpenPosition openPosition){
        openPositionRepository.save(openPosition);
        log.info("Open position added: " + openPosition.toString());
    }

    public void saveClosedPosition(ClosedPosition closedPosition){
        closedPositionRepository.save(closedPosition);
        log.info("Closed position added: " + closedPosition.toString());
    }

    public List<OpenPosition> getOpenPositions(TRADING_TYPE tradingType){
        //binanceService.getAllBaseAssets();
        return openPositionRepository.findAllByOrderBySymbol();
    }

    public List<ClosedPosition> getClosedPositions(TRADING_TYPE tradingType){
        return closedPositionRepository.findAllByOrderBySymbolDate();
    }

}
