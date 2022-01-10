package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.TRADING_TYPE;
import hu.portfoliotracker.Model.OpenPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    @Query("SELECT p FROM OpenPosition p WHERE tradingType = :tradingType ORDER BY symbol, date")
    List<OpenPosition> findAllByTradingType(@Param("tradingType") TRADING_TYPE tradingType);

    OpenPosition findBySymbolAndTradingType(String symbol, TRADING_TYPE tradingType);

    long countBySymbolAndTradingType(String symbol, TRADING_TYPE tradingType);


}
