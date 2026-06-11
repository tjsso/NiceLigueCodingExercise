package niceligue.controller;

import jakarta.validation.Valid;
import java.util.List;
import niceligue.model.Team;
import niceligue.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  @PostMapping
  public ResponseEntity<Team> createTeam(@Valid @RequestBody Team team) {
    return ResponseEntity.ok(teamService.createTeam(team));
  }

  @DeleteMapping("/{name}")
  public ResponseEntity<Void> deleteTeam(@PathVariable String name) {
    teamService.deleteByName(name);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<Page<Team>> getAllTeams(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "name") String sortBy,
    @RequestParam(defaultValue = "asc") String sortDir
  ) {
    // Objective: The list will be paginated and can be sorted server-side (by team name, acronym, and budget).
    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(teamService.findAll(pageable));
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
