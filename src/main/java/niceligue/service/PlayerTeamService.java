package niceligue.service;

import java.util.ArrayList;
import java.util.List;
import niceligue.model.Player;
import niceligue.model.Team;
import niceligue.repository.PlayerRepository;
import niceligue.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerTeamService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * Removes a player from all teams they are currently assigned to.
     * This is necessary before deleting a player to maintain referential integrity.
     */
    public void removePlayerFromAllTeams(Long playerId) {
        playerRepository.findById(playerId).ifPresent(player -> {
            List<Team> teams = new ArrayList<>(player.getTeams());
            for (Team team : teams) {
                team.getPlayers().remove(player);
                // In a real DB, this might require an explicit update if not handled by cascade
            }
            playerRepository.save(player);
        });
    }

    public void addPlayerToTeam(Long playerId, String teamName) {
        playerRepository.findById(playerId).ifPresent(player -> {
            final Team team = teamRepository.findByName(teamName);
            if (team != null && !player.getTeams().contains(team)) {
                player.getTeams().add(team);
                team.getPlayers().add(player);
                playerRepository.save(player);
                teamRepository.save(team);
            }
        });
    }
}
