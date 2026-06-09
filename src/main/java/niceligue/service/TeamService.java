package niceligue.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import niceligue.model.Player;
import niceligue.model.Team;
import niceligue.repository.PlayerRepository;
import niceligue.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerService playerService;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Team findByName(String name) {
        return teamRepository.findByName(name);
    }

    public Optional<Team> findById(String name) {
        return teamRepository.findById(name);
    }

    public void deleteByName(String name) {
        if (teamRepository.existsById(name)) {
            teamRepository.deleteById(name);
        }
    }

    public Team createTeam(Team team) {
        if (team.getPlayers() != null && !team.getPlayers().isEmpty()) {
            for (Player p : team.getPlayers()) {
                if (p.getId() != null && playerService.existsById(p.getId())) {
                    p = playerService.findById(p.getId()).get();
                    team.getPlayers().add(p);
                } else {
                    playerService.save(p);
                    team.getPlayers().add(p);
                }
                if (!p.getTeams().contains(team)) p.getTeams().add(team);
            }
        }
        return teamRepository.save(team);
    }

    public List<Team> search(String pattern) {
        return teamRepository.findAll(
            Specification.where((root, query, cb) ->
                cb.or(
                    cb.like(root.get("name"), "%" + pattern + "%"),
                    cb.like(root.get("abbreviation"), "%" + pattern + "%")
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
                        p.getTeams().add(team);
                        team.getPlayers().add(p);
                    }
                }

                return teamRepository.save(team);
            })
            .orElseThrow(() -> new RuntimeException("Team not found"));
    }
}
