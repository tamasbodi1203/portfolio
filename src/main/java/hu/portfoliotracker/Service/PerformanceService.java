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
        // Keresked??si t??pus alapj??n elk??l??n??tj??k a keresked??seket
        List<Trade> trades = tradeRepository.findByTradingTypeAndDateOrderByDate(tradingType, date, user);
        Iterator<Trade> iterator = trades.iterator();
        while (iterator.hasNext()) {
            Trade t = iterator.next();
            TradingPair tradingPair = tradingPairRepository.findBySymbol(t.getPair());
            String baseAsset = tradingPair.getBaseAsset();
            // V??tel
            if (t.getSide().equals("BUY")){

                // Megn??zz??k, hogy l??tezik-e m??r nyitott sz??mla az adott kriptovalut??val ha nem, akkor nyitunk egyet
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

                // Elad??s
            } else {
                val openPosition = openPositions.stream().filter(e -> e.getSymbol().equals(baseAsset)).findAny().orElseThrow();
                // TODO: ha olyan kriptovalut??t szeretn??nk eladni, amib??l nincs nyitott poz??ci??nk (elad??s el??tt nem volt v??tel), akkor NullPointerException-t dob (Short poz??ci??k lekezel??se)
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

                // Elad??s ut??n ??jra kalkulkul??ljuk a megfelel?? nyitott poz??ci??t, ha nem 0 a h??tramaradt mennyis??g
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
        var gains = BigDecimal.ZERO;
        for (val openPosition: openPositions) {
            val closePrice = binanceService.getLastPriceByDate(openPosition.getSymbol() + "USDT", ms);
            openPosition.setCurrentPrice(closePrice);
            openPosition.setMarketValue(openPosition.getQuantity().multiply(closePrice));
            gains = gains.add(performanceDto.getTotalValue().add(openPosition.getMarketValue().subtract(openPosition.getDeposit())));
        }
        for (val closedPosition: closedPositions) {
            gains = gains.add(closedPosition.getMarketValue().subtract(closedPosition.getDeposit()));
        }
        performanceDto.setTotalValue(gains);
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


    // TODO: A sz??mol??s nem teljes a z??rt poz??ci??k besz??m??t??sa n??lk??l
    public void initAccountSnapshots() {
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Megn??zz??k, hogy az elm??lt 7 nap snapshot-jai megtal??lhat??ak-e az adatb??zisban, ha nem, akkor felvessz??k
        for (int i = 7; i>=1; i--){
            val date = LocalDate.now()
                    .minus(i, ChronoUnit.DAYS);
            log.info(String.valueOf(date));
            if (snapshotRepository.findByDateAndUser(date, user) == null){
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

    public void deleteAllSnapshotsByUser() {
        val user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        snapshotRepository.deleteAllByUser(user);
    }

}