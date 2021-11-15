package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Enum.CURRENCY_PAIR;
import hu.portfoliotracker.Model.OpenPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpenPositionRepository extends JpaRepository<OpenPosition, Long> {

    List<OpenPosition> findAllByOrderByPair();

    @Query("SELECT pair FROM OpenPosition p")
    List<CURRENCY_PAIR> getAllPairs();

    @Query("SELECT p FROM OpenPosition p WHERE p.pair = :pair")
    OpenPosition findByPair(@Param("pair") CURRENCY_PAIR pair);
}
