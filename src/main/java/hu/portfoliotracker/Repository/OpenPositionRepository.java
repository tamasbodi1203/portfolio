package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.OpenPosition;
import hu.portfoliotracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    @Query("SELECT p FROM OpenPosition p WHERE tradingType = :tradingType ORDER BY symbol, date")
    List<OpenPosition> findAllByTradingType(@Param("tradingType") TRADING_TYPE tradingType);

    @Query("SELECT p FROM OpenPosition p WHERE tradingType = :tradingType AND user = :user ORDER BY symbol, date")
    List<OpenPosition> findAllByTradingTypeAndUser(@Param("tradingType") TRADING_TYPE tradingType, @Param("user") User user);

    OpenPosition findBySymbolAndTradingTypeAndUser(String symbol, TRADING_TYPE tradingType, User user);

    long countBySymbolAndTradingTypeAndUser(String symbol, TRADING_TYPE tradingType, User user);

    void deleteAllByUser(User user);

}