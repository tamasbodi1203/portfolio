package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.TradingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingPairRepository extends JpaRepository<TradingPair, Long> {

    public TradingPair findBySymbol(String symbol);
}
