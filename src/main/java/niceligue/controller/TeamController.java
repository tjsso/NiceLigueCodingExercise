package niceligue.controller;

import java.util.List;
import niceligue.model.Team;
import niceligue.service.PlayerTeamService;
import niceligue.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TeamService teamService;

    @Autowired
    private PlayerTeamService playerTeamService;

    @PostMapping
    public Team createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String name) {
        teamService.deleteByName(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.findAll();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Team> getTeamByName(@PathVariable String name) {
        return teamService
            .findById(name)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Team>> search(@RequestParam String pattern) {
        return ResponseEntity.ok(teamService.search(pattern));
    }

    @PutMapping("/{name}")
    public ResponseEntity<Team> updateTeam(
        @PathVariable String name,
        @RequestBody Team teamDetails
    ) {
        return ResponseEntity.ok(teamService.updateTeam(name, teamDetails));
    }
}
