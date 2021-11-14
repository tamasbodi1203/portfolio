package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query("SELECT DISTINCT pair FROM Trade t")
    List<CURRENCY_PAIR> findAllDistinctPair();

    @Query("SELECT SUM(amount) FROM Trade t WHERE pair = :pair AND side = 'BUY'")
    double getAmountBoughtByPair(@Param("pair") CURRENCY_PAIR pair);

    @Query("SELECT SUM(amount) FROM Trade t WHERE pair = :pair AND side = 'SELL'")
    Double getAmountSoldByPair(@Param("pair") CURRENCY_PAIR pair);

    @Query("SELECT SUM(total) FROM Trade t WHERE pair = :pair AND side = 'BUY'")
    double getTotalDepositByPair(@Param("pair") CURRENCY_PAIR pair);

}