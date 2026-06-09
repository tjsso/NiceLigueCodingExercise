package com.niceligue.repository;

import com.niceligue.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository
    extends JpaRepository<Team, String>, JpaSpecificationExecutor<Team>
{
    Team findByName(String name);
}
