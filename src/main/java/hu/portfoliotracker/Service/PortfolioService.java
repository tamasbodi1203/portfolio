package hu.portfoliotracker.Service;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.ClosedPosition;
import hu.portfoliotracker.Model.OpenPosition;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Repository.ClosedPositionRepository;
import hu.portfoliotracker.Repository.OpenPositionRepository;
import hu.portfoliotracker.Repository.TradeRepository;
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
    private TradeRepository tradeRepository;
    @Autowired
    private BinanceService binanceService;

    //FIXME: Deposit nem stimmel
    public void initOpenPositions(){
        List<CURRENCY_PAIR> pairs = tradeRepository.findAllDistinctPair();
        for (CURRENCY_PAIR pair :pairs) {
            double amountBought = tradeRepository.getAmountBoughtByPair(pair);
            Double amountSold = tradeRepository.getAmountSoldByPair(pair) != null ? tradeRepository.getAmountSoldByPair(pair) : 0;
            double quantity = amountBought - amountSold;
            double depositWithoutSell = tradeRepository.getTotalDepositByPair(pair);
            double averageCostBasis =  depositWithoutSell / amountBought;

            double deposit = tradeRepository.getTotalDepositByPair(pair) - (averageCostBasis * amountSold);
            double currentPrice = binanceService.getLastPrice(pair);
            double marketValue = quantity * currentPrice;
            double profit = (currentPrice - averageCostBasis) * quantity;

            OpenPosition openPosition = OpenPosition.builder()
                    .pair(pair)
                    .currentPrice(currentPrice)
                    .deposit(deposit)
                    .quantity(quantity)
                    .averageCostBasis(averageCostBasis)
                    .marketValue(marketValue)
                    .profit(profit)
                    .build();

            List<CURRENCY_PAIR> positionPairs = openPositionRepository.getAllPairs();
            if (!positionPairs.contains(openPosition.getPair())) {
                saveOpenPosition(openPosition);
            } else {
                openPositionRepository.delete(openPositionRepository.findByPair(pair));
                saveOpenPosition(openPosition);
            }
        }
    }

    public void initPositions() {
        openPositionRepository.deleteAll();
        closedPositionRepository.deleteAll();
        List<CURRENCY_PAIR> pairs = tradeRepository.findAllDistinctPair();
        for (CURRENCY_PAIR pair :pairs) {
            List<Trade> trades = tradeRepository.findAllByPairOrderByDate(pair);
            Double averageCostBasis = 0.0;
            Double deposit = 0.0;
            Double quantity = 0.0;

            Iterator<Trade> iterator = trades.iterator();
            while (iterator.hasNext()) {
                Trade t = iterator.next();
                if (t.getSide().equals("BUY")){
                    deposit = deposit + t.getTotal();
                    quantity = quantity + t.getAmount();
                    averageCostBasis = deposit / quantity;
                } else {
                    ClosedPosition closedPosition = ClosedPosition.builder()
                            .pair(pair)
                            .date(t.getDate())
                            .sellPrice(t.getPrice())
                            .deposit(t.getAmount() * averageCostBasis)
                            .quantity(t.getAmount())
                            .averageCostBasis(averageCostBasis)
                            .marketValue(t.getAmount() * t.getPrice())
                            .profit(t.getAmount() * (t.getPrice() - averageCostBasis))
                            .build();
                    saveClosedPosition(closedPosition);
                    deposit -= closedPosition.getDeposit();
                    quantity -= closedPosition.getQuantity();
                    }
                if (!iterator.hasNext()) {
                    // Csak a végső nyitott pozíciót kell tárolnunk
                    Double currentPrice = binanceService.getLastPrice(pair);
                    OpenPosition openPosition = OpenPosition.builder()
                            .pair(pair)
                            .currentPrice(currentPrice)
                            .date(trades.get(0).getDate())
                            .deposit(deposit)
                            .quantity(quantity)
                            .averageCostBasis(averageCostBasis)
                            .marketValue(quantity * currentPrice)
                            .profit((currentPrice - averageCostBasis) * quantity)
                            .build();
                    saveOpenPosition(openPosition);
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

    public List<OpenPosition> getOpenPositions(){
        //initOpenPositions();
        return openPositionRepository.findAllByOrderByPair();
    }

    public List<ClosedPosition> getClosedPositions(){
        //initClosedPositions();
        return closedPositionRepository.findAllByOrderByPairDate();
    }

}
