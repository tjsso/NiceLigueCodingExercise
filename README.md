## Thought process:

### Assumptions
| Assumption      | Decision |
| ----------- | ----------- |
| Brief doesn't specify if a player can play for multiple teams (substitutes, on loan, minor and major teams etc)      | ManyToMany relationship may be more complicated to implement but in reality there are several reasons 1 player may be a part of multiple teams. Therefore 1 Team can have 0..* players, 1 Player can join 0..* Teams      |


I knew Springboot has it's own database initialisation and management and so decided to investigate options there https://www.baeldung.com/java-in-memory-databases

I had previously used H2 for prototyping in the past and used the boilerplate code from [here](https://www.baeldung.com/spring-boot-h2-database) to get started.

Next I created the boilerplate PlayerRepository, TeamRepository, Domain Objects and Controllers.
I know I needed Controllers to handle API calls. I was hoping to avoid a service layer to avoid complexity for such a simple design.

I then followed a TDD approach by writing the following list:
* Creating an empty team
* Creating a player
* Assigning a player to a team
* Creating a team with players, some of whom do not exist (expect it to create the players)
* Deleting a Player (one in a team, one not in a team)
* Deleting a Team (one empty, one with players)
* removing a player from a team (but not deleting either)
* Get Team including listing out all it's players
* Get Player
* Search for player by name
* Search for team by name or abbreviation (same test, search function should check both)
* Updating budget (test negative number (should fail), null, 0, MAX_INT)
* Updating Players Position

Code examples taken from [Medium article](https://medium.com/@premkamalosipalli/unit-testing-spring-boot-rest-apis-with-junit-and-mockito-4aa828be0d52)

And wrote PlayerControllerTest and TeamControllerTest. All tests failed or errored to begin with (as expected).
I focused on PlayerControllerTest first because it should be simpler, a player can or cannot be a part of a team.  
This revealed pretty quickly that my Controller-Service combined layer plan was going to make things difficult because when deleting a Player we also have to remove their reference from any Team's they are apart of.

### Started implementing Service layer.
created TeamService and PlayeService, however whilst trying to implement PlayerController.deletePlayer() I realised that it would be clearer to create a TeamPlayerService, this is a H2 in-memory table to handle the ManyToMany relationship. This service would be better placed to carry out functionality such as removing a Player from all the teams they are currently assigned too.

### Back to PlayerControllerTests
I got all my tests to pass, aside from deletePlayer() because of my complicated Many-To-Many relationship.
1. it caused an infinite loop because: Return a Team, look at its players, serializes a Player, looks at the player's teams, serializes the Team, loop.

There were two fixes to this:
1. Use @ManyToMany(fetch = FetchType.LAZY, mappedBy = "players") on the Teams within Player to LAZY load them, only when required.
2. use @JsonIgnoreProperties("teams"/"players") // When serializing a ManyToMany, ignore their relevant lists

**During investigations of above, found issues because H2 DB is persistent. We do not want it to be for JUnit Tests. Therefore created src/test/resources/application.yaml** to override the H2 DB persistence and make it an in-memory DB only for JUnit tests.
