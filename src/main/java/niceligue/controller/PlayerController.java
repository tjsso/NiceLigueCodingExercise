package niceligue.controller;

import java.util.List;
import niceligue.model.Player;
import niceligue.service.PlayerService;
import niceligue.service.PlayerTeamService;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Endpoints for managing players in the league")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerTeamService playerTeamService;

    @GetMapping
    @Operation(summary = "Get all players", description = "Retrieves a list of all registered players in the league")
    public List<Player> getAllPlayers() {
        return playerService.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a new player", description = "Registers a new player in the system", tags = {
      "Players" })
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return ResponseEntity.ok(playerService.save(player));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID", description = "Retrieves the details of a specific player by their unique identifier")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerService
            .findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing player", description = "Updates the information of an existing player by their unique identifier")
    public ResponseEntity<Player> updatePlayer(
        @PathVariable Long id,
        @RequestBody Player playerDetails) {
      return ResponseEntity.ok(playerService.update(id, playerDetails));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a player", description = "Removes a player from the system, including their associations with all teams")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
      if (playerService.existsById(id)) {
        playerTeamService.removePlayerFromAllTeams(id);
        playerService.deleteById(id);
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search players by name", description = "Finds all players whose names contain the specified search term")
    public ResponseEntity<List<Player>> search(
      @Parameter(description = "The name fragment to search for", required = true) @RequestParam String name) {
        return ResponseEntity.ok(playerService.searchByNameContaining(name));
    }
}
