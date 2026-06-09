package com.niceligue.controller;

import com.niceligue.model.Player;
import com.niceligue.model.Team;
import com.niceligue.repository.TeamRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @PostMapping
    public Team createTeam(@RequestBody Team team) {
        /**
         * TODO - Ideal logic:
         * <ul>
         *   <li>IF player already exists, add them to team</li>
         *   <li>IF player DOES NOT exist, Create them THEN add them to team</li>
         * </ul>
         * How do we know if a player exists or not? Avoid adding duplicate players.
         */
        if (team.getPlayers() != null) {
            for (Player p : team.getPlayers()) {
                p.getTeams().add(team);
            }
        }
        return teamRepository.save(team);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String name) {
        if (teamRepository.existsById(name)) {
            teamRepository.deleteById(name);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Team> getTeamByName(@PathVariable String name) {
        return teamRepository
            .findById(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Team>> search(@RequestParam String pattern) {
        return ResponseEntity.ok(
            teamRepository.findAll(
                Specification.where((root, query, cb) ->
                    cb.or(
                        cb.like(root.get("name"), "%" + pattern + "%"),
                        cb.like(root.get("abbreviation"), "%" + pattern + "%")
                    )
                )
            )
        );
    }

    @PutMapping("/{name}")
    public ResponseEntity<Team> updateTeam(
        @PathVariable String name,
        @RequestBody Team teamDetails
    ) {
        return teamRepository
            .findById(name)
            .map(team -> {
                // The path parameter 'name' is our primary key, so we keep it consistent.
                team.setAbbreviation(teamDetails.getAbbreviation());
                team.setBudget(teamDetails.getBudget());

                // For many-to-many update, we replace the players set.
                if (teamDetails.getPlayers() != null) {
                    team.getPlayers().clear();
                    for (Player p : teamDetails.getPlayers()) {
                        p.getTeams().add(team);
                        team.getPlayers().add(p);
                    }
                }

                return ResponseEntity.ok(teamRepository.save(team));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
