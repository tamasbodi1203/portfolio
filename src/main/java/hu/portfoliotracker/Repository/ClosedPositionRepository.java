package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.ClosedPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClosedPositionRepository extends JpaRepository<ClosedPosition, Long> {

    @Query("SELECT p FROM ClosedPosition p ORDER BY pair, date")
    List<ClosedPosition> findAllByOrderByPairDate();

    @Query("SELECT pair FROM ClosedPosition p")
    List<CURRENCY_PAIR> getAllPairs();

    @Query("SELECT p FROM ClosedPosition p WHERE p.pair = :pair")
    ClosedPosition findByPair(@Param("pair") CURRENCY_PAIR pair);
}
