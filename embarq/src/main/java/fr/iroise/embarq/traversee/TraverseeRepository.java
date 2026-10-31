package fr.iroise.embarq.traversee;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraverseeRepository extends JpaRepository<Traversee, Long> {

    List<Traversee> findByDateHeureAfterOrderByDateHeure(LocalDateTime date);
}
