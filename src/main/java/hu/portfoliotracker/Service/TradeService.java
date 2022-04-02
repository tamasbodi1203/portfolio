package hu.portfoliotracker.Service;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.TradingPair;
import hu.portfoliotracker.Repository.TradeRepository;
import hu.portfoliotracker.Repository.TradingPairRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradingPairRepository tradingPairRepository;

    public void saveTrade(Trade trade){
        trade.setTotal(trade.getPrice() * trade.getAmount());
        tradeRepository.save(trade);
        log.info("Trade added: " + trade.toString());
    }

    public List<Trade> getAllTrades(){
        return tradeRepository.findAllByOrderByDate();
    }

    public List<Trade> getAllByTradingType(TRADING_TYPE tradingType) {
        return tradeRepository.findByTradingTypeOrderByOrderByDate(tradingType);
    }

    public void deleteTrade(long id){
        tradeRepository.deleteById(id);
        log.info("Trade deleted with id: " + Long.toString(id));
    }

    public Trade getTradeById(long id) {
        return tradeRepository.findById(id).orElseThrow(null);
    }

    public void deleteAllTrades(){
        tradeRepository.deleteAll();
    }

    @Transactional
    public void deleteAllTradesByTradingType(TRADING_TYPE tradingType){
        tradeRepository.deleteAllByTradingType(tradingType);
    }

    public List<TradingPair> getAllTradingPairs() {
        return tradingPairRepository.findAllByOrderBySymbol();
    }

}