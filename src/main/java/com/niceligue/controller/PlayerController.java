package com.niceligue.controller;

import com.niceligue.model.Player;
import com.niceligue.repository.PlayerRepository;
import java.util.List;
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
    private PlayerRepository playerRepository;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        // Note: Team association is handled in TeamController or can be set manually here
        return playerRepository.save(player);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerRepository
            .findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(
        @PathVariable Long id,
        @RequestBody Player playerDetails
    ) {
        // TODO - Probably replace with separate methods for updating name, position, and team separately. (team in team management probably?)
        return null;
        // return playerRepository
        //     .findById(id)
        //     .map(player -> {
        //         player.setName(playerDetails.getName());
        //         player.setPosition(playerDetails.getPosition());
        //         // Team association would need to be handled here if changed via this endpoint
        //         if (!player.playsForTeam(playerDetails.getTeams())) {
        //             player.setTeam(playerDetails.getTeam());
        //         }
        //         return ResponseEntity.ok(playerRepository.save(player));
        //     })
        //     .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Player>> search(@RequestParam String pattern) {
        return ResponseEntity.ok(playerRepository.findByNameLike(pattern));
    }
}
