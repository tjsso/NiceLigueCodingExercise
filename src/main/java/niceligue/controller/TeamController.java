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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints for managing teams in the league")
public class TeamController {

  @Autowired
  private TeamService teamService;

  @PostMapping
  @Operation(summary = "Create a new team", description = "Registers a new team in the system")
  public ResponseEntity<Team> createTeam(@Valid @RequestBody Team team) {
    return ResponseEntity.ok(teamService.createTeam(team));
  }

  @DeleteMapping("/{name}")
  @Operation(summary = "Delete a team", description = "Removes a team from the system by its name")
  public ResponseEntity<Void> deleteTeam(@PathVariable String name) {
    teamService.deleteByName(name);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @Operation(summary = "Get all teams with pagination", description = "Retrieves a paginated and sorted list of all teams")
  public ResponseEntity<Page<Team>> getAllTeams(
    @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
    @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
    @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
    @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir) {
    // Objective: The list will be paginated and can be sorted server-side (by team name, acronym, and budget).
    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
      ? Sort.by(sortBy).ascending()
      : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);
    return ResponseEntity.ok(teamService.findAll(pageable));
  }

  @GetMapping("/{name}")
  @Operation(summary = "Get team by name", description = "Retrieves the details of a specific team by its name")
  public ResponseEntity<Team> getTeamByName(@PathVariable String name) {
    return teamService
      .findById(name)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  @Operation(summary = "Search teams", description = "Finds all teams matching the specified search pattern")
  public ResponseEntity<List<Team>> search(
      @Parameter(description = "Search pattern to match against team fields", required = true) @RequestParam String pattern) {
    return ResponseEntity.ok(teamService.search(pattern));
  }

  @PutMapping("/{name}")
  @Operation(summary = "Update a team", description = "Updates the information of an existing team by its name")
  public ResponseEntity<Team> updateTeam(
      @PathVariable String name,
      @Valid @RequestBody Team teamDetails) {
    return ResponseEntity.ok(teamService.updateTeam(name, teamDetails));
  }
}
