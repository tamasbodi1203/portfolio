package hu.portfoliotracker.Service;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.Position;
import hu.portfoliotracker.Repository.PositionRepository;
import hu.portfoliotracker.Repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TradeRepository tradeRepository;

    public void initPositions(){
        List<CURRENCY_PAIR> pairs = tradeRepository.findAllDistinctPair();
        for (CURRENCY_PAIR pair :pairs) {
            double amountBought = tradeRepository.getAmountBoughtByPair(pair);
            Double amountSold = tradeRepository.getAmountSoldByPair(pair) != null ? tradeRepository.getAmountSoldByPair(pair) : 0;
            double quantity = amountBought - amountSold;
            double depositWithoutSell = tradeRepository.getTotalDepositByPair(pair);
            double averageCostBasis =  depositWithoutSell / amountBought;

            double deposit = tradeRepository.getTotalDepositByPair(pair) - (averageCostBasis * amountSold);
            double currentPrice = pair.getNumVal();
            double marketValue = quantity * currentPrice;
            double profit = (currentPrice - averageCostBasis) * quantity;

            Position position = Position.builder()
                    .pair(pair)
                    .currentPrice(currentPrice)
                    .deposit(deposit)
                    .quantity(quantity)
                    .averageCostBasis(averageCostBasis)
                    .marketValue(marketValue)
                    .profit(profit)
                    .build();

            List<CURRENCY_PAIR> positionPairs = positionRepository.getAllPairs();
            if (!positionPairs.contains(position.getPair())) {
                savePosition(position);
            } else {
                positionRepository.delete(positionRepository.findByPair(pair));
                savePosition(position);
            }
        }
    }

    public void savePosition(Position position){
        positionRepository.save(position);
        log.info("Position added: " + position.toString());
    }

    public List<Position> getPositions(){
        initPositions();
        return positionRepository.findAll();
    }

    public void deletePosition(long id){
        positionRepository.deleteById(id);
        log.info("Position deleted with id: " + Long.toString(id));
    }

    public Position getPositionById(long id) {
        return positionRepository.findById(id).orElseThrow(null);
    }
}
