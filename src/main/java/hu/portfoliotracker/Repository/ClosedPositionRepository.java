package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.ClosedPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Long> {

    @Query("SELECT p FROM ClosedPosition p ORDER BY symbol, date")
    List<ClosedPosition> findAllByOrderBySymbolDate();

}
