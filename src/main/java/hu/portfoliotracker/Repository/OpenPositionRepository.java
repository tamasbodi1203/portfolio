package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.OpenPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    List<OpenPosition> findAllByOrderBySymbol();

    OpenPosition findBySymbol(String symbol);

    long countBySymbol(String symbol);
}
