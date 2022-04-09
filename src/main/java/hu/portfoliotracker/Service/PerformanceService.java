package hu.portfoliotracker.Service;

import hu.portfoliotracker.DTO.BalanceDto;
import hu.portfoliotracker.DTO.ClosedPositionDto;
import hu.portfoliotracker.DTO.OpenPositionDto;
import hu.portfoliotracker.DTO.PerformanceDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Model.User;
import hu.portfoliotracker.Repository.CryptocurrencyRepository;
import hu.portfoliotracker.Repository.TradeRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
@Transactional
public class PerformanceService {

    @Autowired
    private TradeRepository tradeRepository;
    @Autowired
    TradingPairRepository tradingPairRepository;
    @Autowired
    private BinanceService binanceService;
    @Autowired
    private CryptocurrencyRepository cryptocurrencyRepository;

    @Transactional
    public PerformanceDto calculatePositionsByDate(TRADING_TYPE tradingType, LocalDateTime date) {
        val ms = ZonedDateTime.of(date, ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
        val performanceDto = new PerformanceDto();
        val openPositions = new ArrayList<OpenPositionDto>();
        val closedPositions = new ArrayList<ClosedPositionDto>();
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Kereskedési típus alapján elkülönítjük a kereskedéseket
        List<Trade> trades = tradeRepository.findByTradingTypeAndDateOrderByDate(tradingType, date, user);
        Iterator<Trade> iterator = trades.iterator();
        while (iterator.hasNext()) {
            Trade t = iterator.next();
            TradingPair tradingPair = tradingPairRepository.findBySymbol(t.getPair());
            String baseAsset = tradingPair.getBaseAsset();
            // Vétel
            if (t.getSide().equals("BUY")){

                // Megnézzük, hogy létezik-e már nyitott számla az adott kriptovalutával ha nem, akkor nyitunk egyet
                if (openPositions.stream().noneMatch(e -> e.getSymbol().equals(baseAsset))) {
                    val closePrice = binanceService.getLastPriceByDate(t.getPair(), ms);
                    Double averageCostBasis = t.getTotal() / t.getAmount();
                    val openPosition = OpenPositionDto.builder()
                            .symbol(tradingPair.getBaseAsset())
                            .cmcId(cryptocurrencyRepository.findByCurrency(tradingPair.getBaseAsset()).getCmcId())
                            .currentPrice(closePrice)
                            .date(t.getDate())
                            .deposit(t.getTotal())
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .marketValue(t.getAmount() * closePrice)
                            .tradingType(tradingType)
                            .build();

                    openPositions.add(openPosition);
                } else {
                    val openPosition = openPositions.stream().filter(e -> e.getSymbol().equals(baseAsset)).findAny().orElseThrow();
                    Double deposit = openPosition.getDeposit() + t.getTotal();
                    Double quantity = openPosition.getQuantity() + t.getAmount();
                    Double averageCostBasis = deposit / quantity;

                    openPosition.setDeposit(deposit);
                    openPosition.setQuantity(quantity);
                    openPosition.setAverageCostBasis(averageCostBasis);
                    openPosition.setMarketValue(openPosition.getQuantity() * openPosition.getCurrentPrice());
                }

                // Eladás
            } else {
                val openPosition = openPositions.stream().filter(e -> e.getSymbol().equals(baseAsset)).findAny().orElseThrow();
                // TODO: ha olyan kriptovalutát szeretnénk eladni, amiből nincs nyitott pozíciónk (eladás előtt nem volt vétel), akkor NullPointerException-t dob (Short pozíciók lekezelése)
                Double averageCostBasis = openPosition.getAverageCostBasis();

                val closedPosition = ClosedPositionDto.builder()
                        .symbol(tradingPair.getSymbol())
                        .date(t.getDate())
                        .sellPrice(t.getPrice())
                        .deposit(t.getAmount() * averageCostBasis)
                        .quantity(t.getAmount())
                        .averageCostBasis(averageCostBasis)
                        .marketValue(t.getAmount() * t.getPrice())
                        .tradingType(tradingType)
                        .build();

                closedPositions.add(closedPosition);

                // Eladás után újra kalkulkuláljuk a megfelelő nyitott pozíciót, ha nem 0 a hátramaradt mennyiség
                if (openPosition.getQuantity() - closedPosition.getQuantity() != 0L) {
                    openPosition.setQuantity(openPosition.getQuantity() - closedPosition.getQuantity());
                    openPosition.setDeposit(openPosition.getDeposit() - closedPosition.getDeposit());
                } else {
                    openPositions.remove(openPosition);
                }
            }
        }

        performanceDto.setLocalDate(date.toLocalDate());
        performanceDto.setTrading_type(tradingType);
        performanceDto.setOpenPositionDtos(openPositions);
        performanceDto.setClosedPositionDtos(closedPositions);
        for (val openPosition: openPositions) {
            performanceDto.setTotalValue(performanceDto.getTotalValue() + openPosition.getMarketValue());
        }
//        for (val closedPosition: closedPositions) {
//            performanceDto.setTotalValue(performanceDto.getTotalValue() + closedPosition.getMarketValue());
//        }
        return performanceDto;
    }

    public void calculatePerformance() {
        val performances = new ArrayList<PerformanceDto>();
        // A mai naptól számított 7 nappal korábbi nap
        LocalDateTime sevenDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(7, ChronoUnit.DAYS);

        val performance7 = calculatePositionsByDate(TRADING_TYPE.SPOT, sevenDaysBefore);
        performances.add(performance7);
        // 6
        LocalDateTime sixDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(6, ChronoUnit.DAYS);

        val performance6 = calculatePositionsByDate(TRADING_TYPE.SPOT, sixDaysBefore);
        performances.add(performance6);
        // 5
        LocalDateTime fiveDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(5, ChronoUnit.DAYS);

        val performance5 = calculatePositionsByDate(TRADING_TYPE.SPOT, fiveDaysBefore);
        performances.add(performance5);
        // 4
        LocalDateTime fourDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(4, ChronoUnit.DAYS);

        val performance4 = calculatePositionsByDate(TRADING_TYPE.SPOT, fourDaysBefore);
        performances.add(performance4);
        // 3
        LocalDateTime threeDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(3, ChronoUnit.DAYS);

        val performance3 = calculatePositionsByDate(TRADING_TYPE.SPOT, threeDaysBefore);
        performances.add(performance3);
        // 2
        LocalDateTime twoDaysBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(2, ChronoUnit.DAYS);

        val performance2 = calculatePositionsByDate(TRADING_TYPE.SPOT, twoDaysBefore);
        performances.add(performance2);
        // 1
        LocalDateTime oneDayBefore = LocalDate.now()
                .atTime(LocalTime.MAX)
                .minus(1, ChronoUnit.DAYS);

        val performance1 = calculatePositionsByDate(TRADING_TYPE.SPOT, oneDayBefore);
        performances.add(performance1);

        System.out.println("valami");
    }

}
