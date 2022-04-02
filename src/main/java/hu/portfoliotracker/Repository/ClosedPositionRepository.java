package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.ClosedPosition;
import hu.portfoliotracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Long> {

    @Query("SELECT p FROM ClosedPosition p WHERE tradingType = :tradingType ORDER BY symbol, date")
    List<ClosedPosition> findAllByTradingType(@Param("tradingType") TRADING_TYPE tradingType);

    @Query("SELECT p FROM ClosedPosition p WHERE tradingType = :tradingType AND user = :user ORDER BY symbol, date")
    List<ClosedPosition> findAllByTradingTypeAndUser(@Param("tradingType") TRADING_TYPE tradingType, @Param("user") User user);

    void deleteAllByUser(User user);

}