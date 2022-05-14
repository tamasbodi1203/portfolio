package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.Trade;
import hu.portfoliotracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findAllByOrderByDate();

    @Modifying
    @Query("DELETE FROM Trade t WHERE tradingType = :tradingType")
    void deleteAllByTradingType(@Param("tradingType") TRADING_TYPE tradingType);

    void deleteAllByTradingTypeAndUser(TRADING_TYPE tradingType, User user);

    //List<Trade> findByTradingTypeByOrderByDate(TRADING_TYPE tradingType);

    @Query("SELECT t FROM Trade t WHERE tradingType = :tradingType AND user = :user ORDER BY t.date")
    List<Trade> findByTradingTypeOrderByDate(@Param("tradingType") TRADING_TYPE tradingType, @Param("user") User user);

    @Query("SELECT t FROM Trade t WHERE tradingType = :tradingType AND date <= :date AND user = :user ORDER BY t.date")
    List<Trade> findByTradingTypeAndDateOrderByDate(@Param("tradingType") TRADING_TYPE tradingType, @Param("date") LocalDateTime date, @Param("user") User user);

}