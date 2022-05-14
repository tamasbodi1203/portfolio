package hu.portfoliotracker.Service;

import hu.portfoliotracker.DTO.BalanceDto;
import hu.portfoliotracker.DTO.ClosedPositionDto;
import hu.portfoliotracker.DTO.OpenPositionDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.*;
import hu.portfoliotracker.Repository.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@Transactional
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
    @Transactional
    public void initPositions(TRADING_TYPE tradingType) {
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Execute method asynchronously. "
                + Thread.currentThread().getName());
        // Kereskedési típus alapján elkülönítjük a kereskedéseket
        List<Trade> trades = tradeRepository.findByTradingTypeOrderByDate(tradingType, user);
        Iterator<Trade> iterator = trades.iterator();
        while (iterator.hasNext()) {
            Trade t = iterator.next();
            TradingPair tradingPair = tradingPairRepository.findBySymbol(t.getPair());
            String baseAsset = tradingPair.getBaseAsset();
            // Vétel
            if (t.getSide().equals("BUY")){

                // Megnézzük, hogy létezik-e már nyitott számla az adott kriptovalutával ha nem, akkor nyitunk egyet
                if (openPositionRepository.countBySymbolAndTradingTypeAndUser(baseAsset, tradingType, user) == 0L) {
                    BigDecimal averageCostBasis = t.getTotal().divide(t.getAmount(), 4, RoundingMode.HALF_UP);
                    OpenPosition openPosition = OpenPosition.builder()
                            .user(user)
                            .symbol(tradingPair.getBaseAsset())
                            .cmcId(cryptocurrencyRepository.findByCurrency(tradingPair.getBaseAsset()).getCmcId())
                            .date(t.getDate())
                            .deposit(t.getTotal())
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .tradingType(tradingType)
                            .build();

                    saveOpenPosition(openPosition);
                } else {
                    OpenPosition openPosition = openPositionRepository.findBySymbolAndTradingTypeAndUser(baseAsset, tradingType, user);
                    BigDecimal deposit = openPosition.getDeposit().add(t.getTotal());
                    BigDecimal quantity = openPosition.getQuantity().add(t.getAmount());
                    BigDecimal averageCostBasis = deposit.divide(quantity, 4, RoundingMode.HALF_UP);

                    openPosition.setDeposit(deposit);
                    openPosition.setQuantity(quantity);
                    openPosition.setAverageCostBasis(averageCostBasis);
                    saveOpenPosition(openPosition);

                }

                // Eladás
            } else {
                OpenPosition openPosition = openPositionRepository.findBySymbolAndTradingTypeAndUser(baseAsset, tradingType, user);
                // TODO: ha olyan kriptovalutát szeretnénk eladni, amiből nincs nyitott pozíciónk (eladás előtt nem volt vétel), akkor NullPointerException-t dob (Short pozíciók lekezelése)
                BigDecimal averageCostBasis = openPosition.getAverageCostBasis();

                ClosedPosition closedPosition = ClosedPosition.builder()
                        .user(user)
                        .symbol(tradingPair.getSymbol())
                        .date(t.getDate())
                        .sellPrice(t.getPrice())
                        .deposit(t.getAmount().multiply(averageCostBasis))
                        .quantity(t.getAmount())
                        .averageCostBasis(averageCostBasis)
                        .marketValue(t.getAmount().multiply(t.getPrice()))
                        .tradingType(tradingType)
                        .build();

                saveClosedPosition(closedPosition);

                // Eladás után újra kalkulkuláljuk a megfelelő nyitott pozíciót, ha nem 0 a hátramaradt mennyiség
                if (!BigDecimal.ZERO.equals(openPosition.getQuantity().subtract(closedPosition.getQuantity()))) {
                    openPosition.setQuantity(openPosition.getQuantity().subtract(closedPosition.getQuantity()));
                    openPosition.setDeposit(openPosition.getDeposit().subtract(closedPosition.getDeposit()));
                    openPositionRepository.save(openPosition);
                } else {
                    openPositionRepository.delete(openPosition);
                }
            }
        }
    }

    public void initBalances() {

        // Töröljük minden futtatáskor a már meglévő pozíciókat, hogy ne duplikálódjanak
        deleteAll();
        initPositions(TRADING_TYPE.SPOT);
        initPositions(TRADING_TYPE.CROSS);
        initPositions(TRADING_TYPE.ISOLATED);

    }

    public void deleteAll(){
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        openPositionRepository.deleteAllByUser(user);
        closedPositionRepository.deleteAllByUser(user);
        log.info("Nyitott és zárt pozíciók törölve");
    }

    public void saveOpenPosition(OpenPosition openPosition){
        openPositionRepository.save(openPosition);
        log.info("Nyitott pozíció hozzáadva: " + openPosition.toString());
    }

    public void saveClosedPosition(ClosedPosition closedPosition){
        closedPositionRepository.save(closedPosition);
        log.info("Zárt pozíció hozzáadva: " + closedPosition.toString());
    }

    public List<OpenPosition> getOpenPositions(TRADING_TYPE tradingType){
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return openPositionRepository.findAllByTradingTypeAndUser(tradingType, user);
    }

    public List<ClosedPosition> getClosedPositions(TRADING_TYPE tradingType){
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return closedPositionRepository.findAllByTradingTypeAndUser(tradingType, user);
    }

    public BalanceDto getPortfolioDto(TRADING_TYPE tradingType) {
        // TODO: Ha már lekérdeztük az árfolyamot egy korábbi pozíciónál, akkor ne kérjük le újra
        HashMap<String, BigDecimal> priceMap = new HashMap<>();
        val openPositionDtos = new ArrayList<OpenPositionDto>();
        val closedPositionDtos = new ArrayList<ClosedPositionDto>();
        val openPositions = getOpenPositions(tradingType);
        val closedPositions = getClosedPositions(tradingType);
        BigDecimal totalOpenDeposit = BigDecimal.valueOf(0);
        BigDecimal totalUnrealizedGains = BigDecimal.valueOf(0);
        BigDecimal totalClosedDeposit = BigDecimal.valueOf(0);
        BigDecimal totalRealizedGains = BigDecimal.valueOf(0);
        for (val openPosition : openPositions) {
            if ((openPosition.getDeposit().compareTo(BigDecimal.ONE) > 0)){
                val currentPrice = binanceService.getLastPrice(openPosition.getSymbol() + "USDT");
                val unrealizedGains = (currentPrice.subtract(openPosition.getAverageCostBasis())).multiply(openPosition.getQuantity());
                val openPositionDto = OpenPositionDto.builder()
                        .symbol(openPosition.getSymbol())
                        .cmcId(openPosition.getCmcId())
                        .currentPrice(currentPrice)
                        .date(openPosition.getDate())
                        .deposit(openPosition.getDeposit())
                        .quantity(openPosition.getQuantity())
                        .averageCostBasis(openPosition.getAverageCostBasis())
                        .marketValue(openPosition.getQuantity().multiply(currentPrice))
                        .unrealizedGains(unrealizedGains)
                        //TODO: 0-val való osztás lekezelése
                        .unrealizedGainsPercent(unrealizedGains.divide(openPosition.getQuantity().multiply(openPosition.getAverageCostBasis()), 4, RoundingMode.HALF_UP))
                        .tradingType(tradingType)
                        .build();

                openPositionDtos.add(openPositionDto);
                totalOpenDeposit = totalOpenDeposit.add(openPositionDto.getDeposit());
                totalUnrealizedGains = totalUnrealizedGains.add(openPositionDto.getUnrealizedGains());
            }
        }
        for (val closedPosition : closedPositions) {
            val realizedGains = closedPosition.getQuantity().multiply(closedPosition.getSellPrice().subtract(closedPosition.getAverageCostBasis()));
            val closedPositionDto = ClosedPositionDto.builder()
                    .symbol(closedPosition.getSymbol())
                    .date(closedPosition.getDate())
                    .sellPrice(closedPosition.getSellPrice())
                    .deposit(closedPosition.getDeposit())
                    .quantity(closedPosition.getQuantity())
                    .averageCostBasis(closedPosition.getAverageCostBasis())
                    .marketValue(closedPosition.getMarketValue())
                    .realizedGains(realizedGains)
                    .realizedGainsPercent(realizedGains.divide(closedPosition.getDeposit(), 4, RoundingMode.HALF_UP))
                    .tradingType(tradingType)
                    .build();

            totalClosedDeposit = totalClosedDeposit.add(closedPosition.getDeposit());
            closedPositionDtos.add(closedPositionDto);
            totalRealizedGains = totalRealizedGains.add(closedPositionDto.getRealizedGains());
        }

        return BalanceDto.builder()
                .openPositionDtos(openPositionDtos)
                .closedPositionDtos(closedPositionDtos)
                .totalDeposit(totalOpenDeposit)
                .totalUnrealizedGains(totalUnrealizedGains)
                .totalUnrealizedGainsPercent(
                        totalUnrealizedGains.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : totalUnrealizedGains.divide(totalOpenDeposit, 4, RoundingMode.HALF_UP)
                )
                .totalRealizedGains(totalRealizedGains)
                .totalRealizedGainsPercent(
                        totalRealizedGains.equals(BigDecimal.ZERO) ? BigDecimal.ZERO :totalRealizedGains.divide(totalClosedDeposit, 4, RoundingMode.HALF_UP)
                )
                .build();
    }

    public List<BalanceDto> refreshPortfolio() {
        log.info("Árfolyamok frissítése");
        long startTime = System.nanoTime();
        val portfolioDtos = new ArrayList<BalanceDto>();
        val spotPortfolioDto = getPortfolioDto(TRADING_TYPE.SPOT);
        val crossPortfolioDto = getPortfolioDto(TRADING_TYPE.CROSS);
        val isolatedPortfolioDto = getPortfolioDto(TRADING_TYPE.ISOLATED);
        portfolioDtos.add(spotPortfolioDto);
        portfolioDtos.add(crossPortfolioDto);
        portfolioDtos.add(isolatedPortfolioDto);
        long stopTime = System.nanoTime();
        long elpasedTime = stopTime - startTime;
        log.info("Árfolyamok frissítése vége: " + String.valueOf(elpasedTime / 1000000000) + " seconds");

        return portfolioDtos;
    }

}