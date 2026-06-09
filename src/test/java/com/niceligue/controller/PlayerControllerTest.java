package com.niceligue.controller;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.niceligue.model.Player;
import org.junit.jupiter.api.BeforeEach;
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
    private com.niceligue.repository.PlayerRepository playerRepository;

    @BeforeEach
    void setup() {
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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Thomas")))
            .andExpect(jsonPath("$.position", is("Midfield")));
    }

    @Test
    @DisplayName("Deleting a Player")
    void testDeletePlayer() throws Exception {
        com.niceligue.model.Team t1 = new com.niceligue.model.Team(
            "Team A",
            "A",
            0.0
        );

        Player p1 = new Player(null, "P1", "Mid");
        p1.getTeams().add(t1);
        playerRepository.save(p1);

        Player p2 = new Player(null, "P2", "Def");
        playerRepository.save(p2);

        mockMvc
            .perform(delete("/api/players/" + p1.getId()))
            .andExpect(status().isNoContent());
        mockMvc
            .perform(delete("/api/players/" + p2.getId()))
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
                jsonPath(
                    "$.content",
                    hasItem(hasEntry("name", "Thomas Muller"))
                )
            );
    }

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
