package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.Cryptocurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Long> {

    //TODO: currency-t átírni symbol-ra
    //@Query("SELECT c.cmcId from CryptoCurrency c WHERE currency = :symbol")
    Cryptocurrency findByCurrency(String symbol);
}
