package niceligue.controller;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import niceligue.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private niceligue.repository.PlayerRepository playerRepository;

    @Autowired
    private niceligue.repository.TeamRepository teamRepository;

    @BeforeEach
    void setup() {
        // Clear associations and entities to avoid DataIntegrityViolation
        // Since we don't have a join table clearer, we can try deleting teams first if they are associated.
        // However, the most direct way is to ensure any players/teams with dependencies are handled.
        // For this specific test environment, clearing teamRepository then playerRepository might work
        // if there's no cascading delete on join table from the DB side.

        // Given the current error, we need to handle the many-to-many association.
        // We can manually clear the teams first.
        teamRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    @DisplayName("Create a player")
    void testCreatePlayer() throws Exception {
        String json = "{\"name\": \"Thomas\", \"position\": \"Midfield\"}";
        mockMvc
            .perform(
                post("/api/players")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Thomas")))
            .andExpect(jsonPath("$.position", is("Midfield")));
    }

    @Test
    @DisplayName("Deleting a Player")
    void testDeletePlayer() throws Exception {
        String teamJson =
            "{\"name\": \"Team A\", \"abbreviation\": \"A\", \"budget\": 0.0, \"players\": [{\"name\": \"P1\", \"position\": \"Mid\"}]}";

        mockMvc
            .perform(
                post("/api/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(teamJson)
            )
            .andExpect(status().isOk());

        // Retrieve the dynamically generated player from the repository
        Player p1 = playerRepository
            .findAll()
            .stream()
            .filter(p -> p.getName().equals("P1"))
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("Player P1 was not saved")
            );

        // Use the dynamic ID for the deletion
        mockMvc
            .perform(delete("/api/players/" + p1.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Get Player")
    void testGetPlayer() throws Exception {
        Player p1 = new Player(null, "Jackson", "Goalie");
        playerRepository.save(p1);

        mockMvc
            .perform(get("/api/players/" + p1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Jackson")));
    }

    @Test
    @DisplayName("Search for player by name")
    void testSearchPlayerByName() throws Exception {
        playerRepository.save(new Player(null, "Thomas Muller", "Forward"));
        playerRepository.save(
            new Player(null, "Robert Lewandowski", "Forward")
        );

        mockMvc
            .perform(get("/api/players/search").param("name", "Muller"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$", hasItem(hasEntry("name", "Thomas Muller")))
            );
    }

    @Disabled
    @Test
    @DisplayName("Updating Players Position")
    void testUpdatePlayerPosition() throws Exception {
        Player p1 = new Player(null, "Thomas", "Midfield");
        playerRepository.save(p1);

        mockMvc
            .perform(
                put("/api/players/" + p1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\": \"Thomas\", \"position\": \"Forward\"}"
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.position", is("Forward")));
    }
}
