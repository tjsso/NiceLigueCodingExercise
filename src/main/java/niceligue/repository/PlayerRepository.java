package niceligue.repository;

import java.util.List;
import niceligue.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository
    extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player>
{
    List<Player> findByNameContaining(String name);
}
