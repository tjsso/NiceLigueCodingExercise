package niceligue.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import niceligue.model.Player;
import niceligue.model.Team;
import niceligue.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TeamService {

  @Autowired
  private TeamRepository teamRepository;

  @Autowired
  private PlayerService playerService;

  public Page<Team> findAll(Pageable pageable) {
    return teamRepository.findAll(pageable);
  }

  public List<Team> findAll() {
    return teamRepository.findAll();
  }

  public Team findByName(String name) {
    return teamRepository.findByName(name);
  }

  @Transactional(readOnly = true)
  public Optional<Team> findById(String name) {
    return teamRepository.findById(name).map(team -> {
      // Load the lazy collection forcing Hibernate to fetch it
      if (team.getPlayers() != null) {
        team.getPlayers().size();
      }
      return team;
    });
  }

  public void deleteByName(String name) {
    if (teamRepository.existsById(name)) {
      teamRepository.deleteById(name);
    }
  }

  public Team createTeam(Team team) {
    if (team.getPlayers() != null && !team.getPlayers().isEmpty()) {
      List<Player> processedPlayers = new ArrayList<>();
      for (Player p : team.getPlayers()) {
        if (p.getId() != null && playerService.existsById(p.getId())) {
          // Player exists, fetch them
          p = playerService.findById(p.getId()).get();
        } else {
          // Player doesn't exist, create them
          p.setId(null);
          p = playerService.save(p);
        }

        if (p.getTeams() == null) p.setTeams(new java.util.HashSet<>());
        p.getTeams().add(team);
        processedPlayers.add(p);
      }
      team.getPlayers().addAll(processedPlayers);
    }

    return teamRepository.save(team);
  }

  public List<Team> search(String pattern) {
    return teamRepository.findAll(
      Specification.where((root, query, cb) ->
        cb.or(
          cb.like(
            cb.lower(root.get("name")),
            "%" + pattern.toLowerCase() + "%"
          ),
          cb.like(
            cb.lower(root.get("abbreviation")),
            "%" + pattern.toLowerCase() + "%"
          )
        )
      )
    );
  }

  public Team updateTeam(String name, Team teamDetails) {
    return teamRepository
      .findById(name)
      .map(team -> {
        team.setAbbreviation(teamDetails.getAbbreviation());
        team.setBudget(teamDetails.getBudget());

        if (teamDetails.getPlayers() != null) {
          team.getPlayers().clear();
          for (Player p : teamDetails.getPlayers()) {
            if (p.getTeams() == null) p.setTeams(new java.util.HashSet<>());
            p.getTeams().add(team);
            team.getPlayers().add(p);
          }
        }

        return teamRepository.save(team);
      })
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
      );
  }
}
