package niceligue.service;

import java.util.ArrayList;
import java.util.List;
import niceligue.model.Player;
import niceligue.model.Team;
import niceligue.repository.PlayerRepository;
import niceligue.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // In order to handle loading of Lazy lists team.getPlayers(), player.getTeams()
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
      }
      playerRepository.save(player);
    });
  }

  /**
   * Master method: Assigns a list of players to a Team object, treating Team as the primary entity.
   */
  public void assignPlayersToTeam(List<Player> players, String teamName) {
    Team team = teamRepository.findByName(teamName);
    if (team != null) {
      for (Player player : players) {
        if (!team.getPlayers().contains(player)) {
          team.getPlayers().add(player);

          if (player.getTeams() == null) player.setTeams(
            new java.util.HashSet<>()
          );
          player.getTeams().add(team);
        }
      }
      teamRepository.save(team);
    }
  }

  public void addPlayerToTeam(Long playerId, String teamName) {
    playerRepository.findById(playerId).ifPresent(player -> {
      assignPlayersToTeam(List.of(player), teamName);
    });
  }
}
