package hu.portfoliotracker.Service;

import hu.portfoliotracker.DTO.ClosedPositionDto;
import hu.portfoliotracker.DTO.OpenPositionDto;
import hu.portfoliotracker.DTO.PortfolioDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.ClosedPosition;
import hu.portfoliotracker.Model.OpenPosition;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        // Kereskedési típus alapján elkülönítjük a kereskedéseket
        List<Trade> trades = tradeRepository.findByTradingTypeOrderByOrderByDate(tradingType);
        Iterator<Trade> iterator = trades.iterator();
        while (iterator.hasNext()) {
            Trade t = iterator.next();
            TradingPair tradingPair = tradingPairRepository.findBySymbol(t.getPair());
            String baseAsset = tradingPair.getBaseAsset();
            // Vétel
            if (t.getSide().equals("BUY")){
                //Double currentPrice = binanceService.getLastPrice(baseAsset + "USDT");

                // Megnézzük, hogy létezik-e már nyitott számla az adott kriptovalutával ha nem, akkor nyitunk egyet
                if (openPositionRepository.countBySymbolAndTradingType(baseAsset, tradingType) == 0L) {
                    Double averageCostBasis = t.getTotal() /t.getAmount();
                    OpenPosition openPosition = OpenPosition.builder()
                            .symbol(tradingPair.getBaseAsset())
                            .cmcId(cryptocurrencyRepository.findByCurrency(tradingPair.getBaseAsset()).getCmcId())
                            .date(trades.get(0).getDate())
                            .deposit(t.getTotal())
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .tradingType(tradingType)
                            .build();

                    saveOpenPosition(openPosition);
                } else {
                    OpenPosition openPosition = openPositionRepository.findBySymbolAndTradingType(baseAsset, tradingType);
                    Double deposit = openPosition.getDeposit() + t.getTotal();
                    Double quantity = openPosition.getQuantity() + t.getAmount();
                    Double averageCostBasis = deposit / quantity;

                    openPosition.setDeposit(deposit);
                    openPosition.setQuantity(quantity);
                    openPosition.setAverageCostBasis(averageCostBasis);
                    saveOpenPosition(openPosition);

                }

                // Eladás
            } else {
                OpenPosition openPosition = openPositionRepository.findBySymbolAndTradingType(baseAsset, tradingType);
                Double averageCostBasis = openPosition.getAverageCostBasis();

                ClosedPosition closedPosition = ClosedPosition.builder()
                        .symbol(tradingPair.getSymbol())
                        .date(t.getDate())
                        .sellPrice(t.getPrice())
                        .deposit(t.getAmount() * averageCostBasis)
                        .quantity(t.getAmount())
                        .averageCostBasis(averageCostBasis)
                        .marketValue(t.getAmount() * t.getPrice())
                        .tradingType(tradingType)
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

    public void deleteAll(){
        openPositionRepository.deleteAll();
        closedPositionRepository.deleteAll();
        log.info("Nyitott és zárt pozíciók törölve");
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
        return openPositionRepository.findAllByTradingType(tradingType);
    }

    public List<ClosedPosition> getClosedPositions(TRADING_TYPE tradingType){
        return closedPositionRepository.findAllByTradingType(tradingType);
    }

    public PortfolioDto getPortfolioDto(TRADING_TYPE tradingType) {
        val openPositionDtos = new ArrayList<OpenPositionDto>();
        val closedPositionDtos = new ArrayList<ClosedPositionDto>();
        val openPositions = getOpenPositions(tradingType);
        val closedPositions = getClosedPositions(tradingType);
        Double totalOpenDeposit = Double.valueOf(0);
        Double totalUnrealizedGains = Double.valueOf(0);
        Double totalClosedDeposit = Double.valueOf(0);
        Double totalRealizedGains = Double.valueOf(0);
        for (val openPosition : openPositions) {
            val currentPrice = binanceService.getLastPrice(openPosition.getSymbol() + "USDT");
            val unrealizedGains = (currentPrice - openPosition.getAverageCostBasis()) * openPosition.getQuantity();
            val openPositionDto = OpenPositionDto.builder()
                    .symbol(openPosition.getSymbol())
                    .cmcId(openPosition.getCmcId())
                    .currentPrice(currentPrice)
                    .date(openPosition.getDate())
                    .deposit(openPosition.getDeposit())
                    .quantity(openPosition.getQuantity())
                    .averageCostBasis(openPosition.getAverageCostBasis())
                    .marketValue(openPosition.getQuantity() * currentPrice)
                    .unrealizedGains(unrealizedGains)
                    //TODO: 0-val való osztás lekezelése
                    .unrealizedGainsPercent(unrealizedGains / (openPosition.getQuantity() * openPosition.getAverageCostBasis()))
                    .tradingType(tradingType)
                    .build();

            openPositionDtos.add(openPositionDto);
            totalOpenDeposit += openPositionDto.getDeposit();
            totalUnrealizedGains += openPositionDto.getUnrealizedGains();
        }
        for (val closedPosition : closedPositions) {
            val realizedGains = closedPosition.getQuantity() * (closedPosition.getSellPrice() - closedPosition.getAverageCostBasis());
            val closedPositionDto = ClosedPositionDto.builder()
                    .symbol(closedPosition.getSymbol())
                    .date(closedPosition.getDate())
                    .sellPrice(closedPosition.getSellPrice())
                    .deposit(closedPosition.getDeposit())
                    .quantity(closedPosition.getQuantity())
                    .averageCostBasis(closedPosition.getAverageCostBasis())
                    .marketValue(closedPosition.getMarketValue())
                    .realizedGains(realizedGains)
                    .realizedGainsPercent(realizedGains / closedPosition.getDeposit())
                    .tradingType(tradingType)
                    .build();

            totalClosedDeposit += closedPosition.getDeposit();
            closedPositionDtos.add(closedPositionDto);
            totalRealizedGains += closedPositionDto.getRealizedGains();
        }

        val portfolioDto = PortfolioDto.builder()
                .openPositionDtos(openPositionDtos)
                .closedPositionDtos(closedPositionDtos)
                .totalDeposit(totalOpenDeposit)
                .totalUnrealizedGains(totalUnrealizedGains)
                .totalUnrealizedGainsPercent(totalUnrealizedGains / totalOpenDeposit)
                .totalRealizedGains(totalRealizedGains)
                .totalRealizedGainsPercent(totalRealizedGains / totalClosedDeposit)
                .build();

        return portfolioDto;
    }

}
