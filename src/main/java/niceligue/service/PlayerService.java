package niceligue.service;

import java.util.List;
import java.util.Optional;
import niceligue.model.Player;
import niceligue.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    public List<Player> searchByNameContaining(String name) {
        return playerRepository.findByNameContaining(name);
    }

    public void deleteById(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        }
    }

    public boolean existsById(Long id) {
        System.out.println(
            "existsById : " +
                id +
                " result : " +
                playerRepository.existsById(id)
        );
        return playerRepository.existsById(id);
    }
}
