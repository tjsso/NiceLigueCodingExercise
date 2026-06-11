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

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerTeamService playerTeamService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.findAll();
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        return ResponseEntity.ok(playerService.save(player));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerService
            .findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(
        @PathVariable Long id,
        @RequestBody Player playerDetails) {
      return ResponseEntity.ok(playerService.update(id, playerDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
      if (playerService.existsById(id)) {
        playerTeamService.removePlayerFromAllTeams(id);
        playerService.deleteById(id);
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Player>> search(@RequestParam String name) {
        return ResponseEntity.ok(playerService.searchByNameContaining(name));
    }
}
