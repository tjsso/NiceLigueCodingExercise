package com.niceligue.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.niceligue.model.Player;
import com.niceligue.model.Team;
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
public class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.niceligue.repository.TeamRepository teamRepository;

    @Autowired
    private com.niceligue.repository.PlayerRepository playerRepository;

    @BeforeEach
    void setup() {
        teamRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    @DisplayName("Assign a player to a team")
    void testAssignPlayerToTeam() throws Exception {
        mockMvc.perform(
            post("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": 50000.0}"
                )
        );

        Player p = new Player(null, "Jackson", "Goalie");
        Team t = teamRepository.findByName("Nice AFC");
        p.getTeams().add(t);
        playerRepository.save(p);

        mockMvc
            .perform(get("/api/teams/" + t.getName()))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.players", hasItem(hasEntry("name", "Jackson")))
            );
    }

    @Test
    @DisplayName("Create an empty team")
    void testCreateEmptyTeam() throws Exception {
        String json =
            "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": 55000.0}";
        mockMvc
            .perform(
                post("/api/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Nice AFC")))
            .andExpect(jsonPath("$.players", empty()));
    }

    @Test
    @DisplayName(
        "Create a team with players, some of whom do not exist (expect it to create the players)"
    )
    void testCreateTeamWithNewPlayers() throws Exception {
        // This test requires logic in TeamController to check for existence and save new ones.
        String json =
            "{" +
            "\"name\": \"Real Madrid\", " +
            "\"abbreviation\": \"RMA\", " +
            "\"budget\": 600000.0, " +
            "\"players\": [" +
            "  {\"id\": 99, \"name\": \"Existing Player\", \"position\": \"Forward\"}, " + // This might exist or not
            "  {\"id\": null, \"name\": \"New Player\", \"position\": \"Midfield\"}" + // This should be created if unknown
            "]" +
            "}";

        mockMvc
            .perform(
                post("/api/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.players", hasItem(hasEntry("name", "New Player")))
            );
    }

    @Test
    @DisplayName("Deleting a Team (one empty, one with players)")
    void testDeleteTeam() throws Exception {
        teamRepository.save(new Team("Empty Team", "ET", 0.0));

        Team t2 = new Team("Full Team", "FT", 100.0);
        Player p3 = new Player(null, "P3", "GK");
        p3.getTeams().add(t2);
        playerRepository.save(p3);
        teamRepository.save(t2);

        mockMvc
            .perform(delete("/api/teams/Empty Team"))
            .andExpect(status().isNoContent());
        mockMvc
            .perform(delete("/api/teams/Full Team"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("removing a player from a team (but not deleting either)")
    void testRemovePlayerFromTeam() throws Exception {
        Team t1 = new Team("Team 1", "T1", 100.0);
        teamRepository.save(t1);

        Player p1 = new Player(null, "P1", "Mid");
        p1.getTeams().add(t1);
        playerRepository.save(p1);

        // The controller update logic should handle clearing the set or removing specific players
        String json =
            "{\"name\": \"Team 1\", \"abbreviation\": \"T1\", \"budget\": 100.0, \"players\": []}";
        mockMvc
            .perform(
                put("/api/teams/Team 1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players", empty()));
    }

    @Test
    @DisplayName("Get Team including listing out all it's players")
    void testGetTeamWithPlayers() throws Exception {
        Team t1 = new Team("Nice AFC", "OGC", 55000.0);
        teamRepository.save(t1);

        Player p1 = new Player(null, "Thomas", "Midfield");
        p1.getTeams().add(t1);
        playerRepository.save(p1);

        mockMvc
            .perform(get("/api/teams/Nice AFC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players", hasSize(1)))
            .andExpect(jsonPath("$.players[0].name", is("Thomas")));
    }

    @Test
    @DisplayName("Search for team by name or abbreviation")
    void testSearchTeam() throws Exception {
        teamRepository.save(new Team("Yogscast", "YGC", 1000.0));
        teamRepository.save(new Team("Nice AFC", "OGS", 5000.0));

        mockMvc
            .perform(get("/api/teams/search").param("query", "OGS"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content", hasItem(hasEntry("abbreviation", "OGS")))
            );
    }

    @Test
    @DisplayName("Updating budget (test negative number, null, 0, MAX_INT)")
    void testUpdateBudget() throws Exception {
        teamRepository.save(new Team("Nice AFC", "OGC", 50000.0));

        // Negative Budget -> Should fail with 406 Not Acceptable
        mockMvc
            .perform(
                put("/api/teams/Nice AFC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": -100.0}"
                    )
            )
            .andExpect(status().isNotAcceptable());

        // Null Budget -> Should fail with 400 Bad Request
        mockMvc
            .perform(
                put("/api/teams/Nice AFC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": null}"
                    )
            )
            .andExpect(status().isBadRequest());

        // Zero Budget -> Should succeed
        mockMvc
            .perform(
                put("/api/teams/Nice AFC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": 0.0}"
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budget", is(0.0)));

        Double maxBudget = Double.MAX_VALUE;
        mockMvc
            .perform(
                put("/api/teams/Nice AFC")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        String.format(
                            "{\"name\": \"Nice AFC\", \"abbreviation\": \"OGC\", \"budget\": %f}",
                            maxBudget
                        )
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.budget", is(maxBudget)));
    }
}
