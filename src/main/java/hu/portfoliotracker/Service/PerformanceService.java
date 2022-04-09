package hu.portfoliotracker.Service;

import hu.portfoliotracker.DTO.ClosedPositionDto;
import hu.portfoliotracker.DTO.OpenPositionDto;
import hu.portfoliotracker.DTO.PerformanceDto;
import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Snapshot;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Model.User;
import hu.portfoliotracker.Repository.CryptocurrencyRepository;
import hu.portfoliotracker.Repository.SnapshotRepository;
import hu.portfoliotracker.Repository.TradeRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private TradingPairRepository tradingPairRepository;
    @Autowired
    private CryptocurrencyRepository cryptocurrencyRepository;
    @Autowired
    private SnapshotRepository snapshotRepository;
    @Autowired
    private BinanceService binanceService;

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
                    BigDecimal averageCostBasis = t.getTotal().divide(t.getAmount(), 4, RoundingMode.HALF_UP);
                    val openPosition = OpenPositionDto.builder()
                            .symbol(tradingPair.getBaseAsset())
                            .cmcId(cryptocurrencyRepository.findByCurrency(tradingPair.getBaseAsset()).getCmcId())
                            .date(t.getDate())
                            .deposit(t.getTotal())
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .tradingType(tradingType)
                            .build();

                    openPositions.add(openPosition);
                } else {
                    val openPosition = openPositions.stream().filter(e -> e.getSymbol().equals(baseAsset)).findAny().orElseThrow();
                    BigDecimal deposit = openPosition.getDeposit().add(t.getTotal());
                    BigDecimal quantity = openPosition.getQuantity().add(t.getAmount());
                    BigDecimal averageCostBasis = deposit.divide(quantity, 4, RoundingMode.HALF_UP);

                    openPosition.setDeposit(deposit);
                    openPosition.setQuantity(quantity);
                    openPosition.setAverageCostBasis(averageCostBasis);
                }

                // Eladás
            } else {
                val openPosition = openPositions.stream().filter(e -> e.getSymbol().equals(baseAsset)).findAny().orElseThrow();
                // TODO: ha olyan kriptovalutát szeretnénk eladni, amiből nincs nyitott pozíciónk (eladás előtt nem volt vétel), akkor NullPointerException-t dob (Short pozíciók lekezelése)
                BigDecimal averageCostBasis = openPosition.getAverageCostBasis();

                val closedPosition = ClosedPositionDto.builder()
                        .symbol(tradingPair.getSymbol())
                        .date(t.getDate())
                        .sellPrice(t.getPrice())
                        .deposit(t.getAmount().multiply(averageCostBasis))
                        .quantity(t.getAmount())
                        .averageCostBasis(averageCostBasis)
                        .marketValue(t.getAmount().multiply( t.getPrice()))
                        .tradingType(tradingType)
                        .build();

                closedPositions.add(closedPosition);

                // Eladás után újra kalkulkuláljuk a megfelelő nyitott pozíciót, ha nem 0 a hátramaradt mennyiség
                if (!openPosition.getQuantity().subtract(closedPosition.getQuantity()).equals(BigDecimal.ZERO)) {
                    openPosition.setQuantity(openPosition.getQuantity().subtract(closedPosition.getQuantity()));
                    openPosition.setDeposit(openPosition.getDeposit().subtract(closedPosition.getDeposit()));
                } else {
                    openPositions.remove(openPosition);
                }
            }
        }

        performanceDto.setDate(date.toLocalDate());
        performanceDto.setTrading_type(tradingType);
        performanceDto.setOpenPositionDtos(openPositions);
        performanceDto.setClosedPositionDtos(closedPositions);
        BigDecimal totalClosedDeposit = BigDecimal.valueOf(0);
        BigDecimal totalRealizedGains = BigDecimal.valueOf(0);
        for (val openPosition: openPositions) {
            val closePrice = binanceService.getLastPriceByDate(openPosition.getSymbol() + "USDT", ms);
            openPosition.setCurrentPrice(closePrice);
            openPosition.setMarketValue(openPosition.getQuantity().multiply(closePrice));
            performanceDto.setTotalValue(performanceDto.getTotalValue().add(openPosition.getMarketValue()));
        }
//        for (val closedPosition: closedPositions) {
//            totalClosedDeposit = totalClosedDeposit.add(closedPosition.getDeposit());
//            totalRealizedGains = totalRealizedGains.add(closedPosition.getMarketValue().subtract(closedPosition.getDeposit()));
//        }
//        BigDecimal kulonbseg = totalClosedDeposit.add(totalRealizedGains);
//        performanceDto.setTotalValue(performanceDto.getTotalValue().add(kulonbseg));
        return performanceDto;
    }

    public List<PerformanceDto> calculateDailySnapshots(LocalDate date) {
        val dailyAccountSnapshot = new ArrayList<PerformanceDto>();
        val dateTime = date.atTime(LocalTime.MAX);

        val spotSnapshot = calculatePositionsByDate(TRADING_TYPE.SPOT, dateTime);
        dailyAccountSnapshot.add(spotSnapshot);
        val crossSnapshot = calculatePositionsByDate(TRADING_TYPE.CROSS, dateTime);
        dailyAccountSnapshot.add(crossSnapshot);
        val isolatedSnapshot = calculatePositionsByDate(TRADING_TYPE.ISOLATED, dateTime);
        dailyAccountSnapshot.add(isolatedSnapshot);


        return dailyAccountSnapshot;
    }


    // TODO: A számolás nem teljes a zárt pozíciók beszámítása nélkül
    public void initAccountSnapshots() {
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Megnézzük, hogy az elmúlt 7 nap snapshot-jai megtalálhatóak-e az adatbázisban, ha nem, akkor felvesszük
        for (int i = 7; i>=1; i--){
            val date = LocalDate.now()
                    .minus(i, ChronoUnit.DAYS);
            if (snapshotRepository.findByDate(date) == null){
                val dailyAccountSnapshot = calculateDailySnapshots(date);
                BigDecimal totalValue = BigDecimal.ZERO;
                for (val performanceDto : dailyAccountSnapshot) {
                    totalValue = totalValue.add(performanceDto.getTotalValue());
                }
                val snapshot = Snapshot.builder()
                        .date(dailyAccountSnapshot.get(0).getDate())
                        .accountTotal(totalValue)
                        .user(user)
                        .build();
                snapshotRepository.save(snapshot);
            }
        }
    }

    public List<Snapshot> getLastSevenDays() {
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long startTime = System.nanoTime();
        initAccountSnapshots();
        long stopTime = System.nanoTime();
        long elpasedTime = stopTime - startTime;
        log.info("Snapshot init run time: " + String.valueOf(elpasedTime / 1000000000) + " seconds");
        return snapshotRepository.findAllByUser(user);
    }

}