package com.niceligue.repository;

import com.niceligue.model.Player;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository
    extends JpaRepository<Player, Long>, JpaSpecificationExecutor<Player>
{
    List<Player> findByNameLike(String name);
}
