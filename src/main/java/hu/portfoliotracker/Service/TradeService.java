package hu.portfoliotracker.Service;

import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TradeService {

    private TradeRepository tradeRepository;

    @Autowired
    public void setTradeRepository(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void addTrade(Trade trade){
        tradeRepository.addTrade(trade);
        log.info("Trade added: " + trade.toString());
    }

    public List<Trade> getTrades(){
        return tradeRepository.getTrades();
    }

    public void deleteTrade(long id){
        tradeRepository.deleteTrade(id);
        log.info("Trade deleted with id: " + Long.toString(id));
    }

    public Trade getTradeById(long id) {
        return tradeRepository.getTradeById(id);
    }

    public void saveTrade(Trade trade) {
        tradeRepository.saveTrade(trade);
    }
}
