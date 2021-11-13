package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.Trade;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TradeRepository {

    private static long currentId = 0L;
    private List<Trade> trades = new ArrayList<>();

    public void addTrade(Trade trade){
        trade.setId(currentId++);
        trade.setTotal(trade.getPrice() * trade.getAmount());
        trades.add(trade);
    }

    public List<Trade> getTrades(){
        return trades;
    }

    public void deleteTrade(long id){
        trades.removeIf(trade -> trade.getId() == id);
    }

    public Trade getTradeById(long id){
        Optional<Trade> trade = trades.stream().filter(t -> t.getId() == id).findAny();
        return trade.orElse(null);
    }

    public void saveTrade(Trade trade) {
        Optional<Trade> existing = trades.stream().filter(t -> t.getId().equals(trade.getId())).findAny();
        if (existing.isPresent()){
            existing.get().setDate(trade.getDate());
            existing.get().setPair(trade.getPair());
            existing.get().setSide(trade.getSide());
            existing.get().setPrice(trade.getPrice());
            existing.get().setAmount(trade.getAmount());
            existing.get().setTotal(trade.getPrice() * trade.getAmount());
        }
    }
}