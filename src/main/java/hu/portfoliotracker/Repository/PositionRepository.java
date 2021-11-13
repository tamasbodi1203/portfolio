package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query("SELECT pair FROM Position p")
    List<CURRENCY_PAIR> getAllPairs();

    @Query("SELECT position FROM Position position WHERE position.pair = :pair")
    Position findByPair(@Param("pair") CURRENCY_PAIR pair);
}
