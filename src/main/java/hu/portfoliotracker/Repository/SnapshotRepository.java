package hu.portfoliotracker.Repository;

import hu.portfoliotracker.Model.Snapshot;
import hu.portfoliotracker.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SnapshotRepository extends JpaRepository<Snapshot, Long> {

    List<Snapshot> findAllByUser(User user);

    Snapshot findByDate(LocalDate date);
}
