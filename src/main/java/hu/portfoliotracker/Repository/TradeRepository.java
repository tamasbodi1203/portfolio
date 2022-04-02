package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findAllByOrderByDate();

    @Modifying
    @Query("DELETE FROM Trade t WHERE tradingType = :tradingType")
    void deleteAllByTradingType(@Param("tradingType") TRADING_TYPE tradingType);

    //List<Trade> findByTradingTypeByOrderByDate(TRADING_TYPE tradingType);

    @Query("SELECT t FROM Trade t WHERE tradingType = :tradingType ORDER BY t.date")
    List<Trade> findByTradingTypeOrderByOrderByDate(@Param("tradingType") TRADING_TYPE tradingType);

    //TODO: Legacy
    @Query("SELECT t FROM Trade t WHERE pair = :pair ORDER BY t.date")
    List<Trade> findAllByPairOrderByDate(@Param("pair") String pair);

    @Query("SELECT DISTINCT pair FROM Trade t")
    List<String> findAllDistinctPair();

    @Query("SELECT DISTINCT pair FROM Trade t WHERE side = 'SELL'")
    List<String> findAllDistinctPairClosed();

    @Query("SELECT t FROM Trade t WHERE pair = :pair AND side = 'SELL'")
    List<Trade> findAllSellTrade(@Param("pair") String pair);

    @Query("SELECT SUM(amount) FROM Trade t WHERE pair = :pair AND side = 'BUY'")
    double getAmountBoughtByPair(@Param("pair") String pair);

    @Query("SELECT SUM(amount) FROM Trade t WHERE pair = :pair AND side = 'SELL'")
    Double getAmountSoldByPair(@Param("pair") String pair);

    @Query("SELECT SUM(total) FROM Trade t WHERE pair = :pair AND side = 'BUY'")
    double getTotalDepositByPair(@Param("pair") String pair);

}