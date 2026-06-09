package com.niceligue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String position;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "player_teams",
        joinColumns = @JoinColumn(name = "player_id"),
        inverseJoinColumns = @JoinColumn(name = "team_name")
    )
    private Set<Team> teams = new HashSet<>();

    public Player() {}

    public Player(Long id, String name, String position) {
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }

    public boolean playsForTeam(String teamName) {
        return (
            null !=
            teams
                .stream()
                .filter(team -> team.getName().equals(teamName))
                .findFirst()
                .orElse(null)
        );
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Player
            ? this.getId().equals(((Player) o).getId())
            : false;
    }

    @Override
    public String toString() {
        return (
            "Player{" +
            "id=" +
            id +
            ", name='" +
            name +
            '\'' +
            ", position='" +
            position +
            '\'' +
            '}'
        );
    }
}
