## time taken 
total: 15 hours

## How to run and test this application

To run and test this application, you have two main options:

### Using `mvn spring-boot:run` and Swagger UI
1. [Checkout project](https://github.com/tjsso/NiceLigueCodingExercise)
2. open a terminal within project directory
3. Run the following command in your terminal:
   ```bash
   mvn spring-boot:run
   ```
4. Once the application has started, navigate to the Swagger UI at:
   https://localhost:8080/swagger-ui

### Using GitHub Codespaces
If this application is running via a GitHub Codespace, you can access the Swagger UI directly using the provided URL:
[https://fictional-chainsaw-54qw949grw5cv4r6-8080.app.github.dev/swagger-ui/index.html](https://fictional-chainsaw-54qw949grw5cv4r6-8080.app.github.dev/swagger-ui/index.html)*

*Double check URL via the github repository link. Possible it has updated/changed.

I did experience another issue where the request URL was incorrect. For Github codespaces it was specifically being set to localhost:8080 which is not the host URL for github code spaces. I found the answer on [stack overflow](https://stackoverflow.com/questions/60625494/wrong-generated-server-url-in-springdoc-openapi-ui-swagger-ui-deployed-behin)

---

## Thought process:

### Assumptions
| Assumption      | Decision |
| ----------- | ----------- |
| Brief doesn't specify if a player can play for multiple teams (substitutes, on loan, minor and major teams etc)      | ManyToMany relationship may be more complicated to implement but in reality there are several reasons 1 player may be a part of multiple teams. Therefore 1 Team can have 0..* players, 1 Player can join 0..* Teams      |


I knew Springboot has it's own database initialisation and management and so decided to investigate options there [https://www.baeldung.com/java-in-memory-databases]

I had previously used H2 for prototyping in the past and used the boilerplate code from [here](https://www.baeldung.com/spring-boot-h2-database) to get started.

Next I created the boilerplate [PlayerRepository](src/main/java/niceligue/repository/PlayerRepository.java), [TeamRepository](src/main/java/niceligue/repository/TeamRepository.java), Domain Objects and Controllers.
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

And wrote [PlayerControllerTest](src/test/java/niceligue/controller/PlayerControllerTest.java) and [TeamControllerTest](src/test/java/niceligue/controller/TeamControllerTest.java). All tests failed or errored to begin with (as expected).
I focused on PlayerControllerTest first because it should be simpler, a player can or cannot be a part of a team.  
This revealed pretty quickly that my Controller-Service combined layer plan was going to make things difficult because when deleting a Player we also have to remove their reference from any Team's they are apart of.

### Started implementing Service layer.
created [TeamService](src/main/java/niceligue/service/TeamService.java) and [PlayerService](src/main/java/niceligue/service/PlayerService.java), however whilst trying to implement [PlayerController](src/main/java/niceligue/controller/PlayerController.java).deletePlayer() I realised that it would be clearer to create a [TeamPlayerService](src/main/java/niceligue/service/PlayerTeamService.java), this is a H2 in-memory table to handle the ManyToMany relationship. This service would be better placed to carry out functionality such as removing a Player from all the teams they are currently assigned too.

### Back to PlayerControllerTests
I got all my tests to pass, aside from deletePlayer() because of my complicated Many-To-Many relationship.
1. it caused an infinite loop because: Return a Team, look at its players, serializes a Player, looks at the player's teams, serializes the Team, loop.

There were two fixes to this:
1. Use @ManyToMany(fetch = FetchType.LAZY, mappedBy = "players") on the Teams within Player to LAZY load them, only when required.
2. use @JsonIgnoreProperties("teams"/"players") // When serializing a ManyToMany, ignore their relevant lists

**During investigations of above, found issues because H2 DB is persistent. We do not want it to be for JUnit Tests. Therefore created src/test/resources/application.yaml** to override the H2 DB persistence and make it an in-memory DB only for JUnit tests.

### Implement TeamControllerTests

After PlayerControllerTests were all passing successfully I turned my attention to the failing TeamControllerTests. Most of these failures were self-explanatory: For testUpdateBudget I wanted to do a fancier Response code and reason however after some investigation I moved this outside of scope of this project. I wanted to keep it simple, this would have required creation of a custom Exception and/or CustomExceptionHandling. 

One of the issues I encounter was the testSearchTeam(), This should have been a simple test but I came across two issues. I did not realise that Specification -> CriteriaBuilder.like are case-sensitive (LIKE in SQL is case-insensitive and expected that to carry over). I decided to use Specification for the implementation of search because the alternative would have been a very wordy Spring data method such as `findByNameContainingIgnoreCaseOrAbbreviationContainingIgnoreCase` which hurts my eyes to read, never mind being a mouthful. 

The other issue I encountered with search was again, because it was such a simple method when my test was returning 400 instead of 200 I could not understand why. I spent approximately 1 hour trying to diagnose this issue before eventually uploading my code to an LLM. (See LLM Section Below)

### Back to the brief

After getting these tests working successfully. I returned to the brief and acknowledged the two main objectives:
1. One that returns a list of teams, each containing a list of players. 
  - The list will be paginated and can be sorted server-side (by team name, acronym, and budget).
    
2. Another that allows adding a team with or without associated players (all other fields are required).

For 1., during my JUnit testing of my TeamControllerTest I realised that I needed @Transactional annotations in order to keep the data connection open for hibernate to load the lazy lists as requested. I also needed to return to my [TeamController](src/main/java/niceligue/controller/TeamController.java) to add pagination and sorting, this was the least of my worries as i've had plenty of experience and knew how to set it up once I knew findAll() worked via JUnit test.

For 2., this was implemented from the start, I did add an @Valid to my Controller method to ensure that the validation annotations were called when something was not correct.

## Deployment

When thinking about how to run this software for testing/prototyping one main thought came to mind: Use a swagger open api web page. It is automatically generated by _springdoc-openapi-starter-webmvc-ui_ and allows someone to use and interact with each endpoint without the overhead of needing to build a GUI or relying on the user to use a terminal interface.

How do we "host" the swagger UI however? The obvious answer is to run `mvn spring-boot:run` from within this codebase and then navigate to https://localhost:8080/swagger-ui
Perhaps a better alternative; I have had multiple experiences of peers asking me to "test something out" for them and I find out it is hosted through github codespaces. I've known this functionality exists but never used it myself. I started by reading the github documentation and created a .devcontainer/devcontainer.json however this was proving to be a bit of a learning curve for myself in the limited time for this challenge and was not an effective use of my time. Therefore I utilsed an LLM to assist in getting this working (See LLM usage section below for more info).

##

After testing deployment via github codespaces I ran into an issue with CORS because spring boot by default only accepts incoming requests from `localhost`. 
I have encountered this issue before and used [baeldung's guide](https://www.baeldung.com/spring-webflux-cors) to set `.allowedOriginPatterns("https://*.app.github.dev")`; This is enforcing security as it refuses connections from non github hosts.

## LLM (AI) usage
The aims of this project are not to automatically generate code using an LLM. However in today's landscape it is hard to avoid them. Therefore this section highlights any specific usages of LLM "AI" to get me an answer.
| Problem      | Usage/Output |
| ----------- | ----------- |
| When thinking about how to "host" my solution. I remembered previously using GitHub Codespaces to prototype NodeJS web apps for prototyping. However I have never personally set this up. I attempted to follow [this documentation](https://docs.github.com/en/codespaces/setting-up-your-project-for-codespaces/adding-a-dev-container-configuration/setting-up-your-java-project-for-codespaces)      | I ended up turn to an LLM (Gemini Flash) for assistance with the creation of the devcontainer.json. I had problems with the container not compiling specific versions of java and it also informed me how to `reload container` after updating the devcontainer.json file. For all intents and purposes .devcontainer/ folder and contents can be considered AI generated. |
| Had an issue with my TeamControllerTest.testSearchTeam()    | I spent far too long investigating it before copy and pasting the methods TeamControllerTest.testSearchTeam() and TeamController.search() into Gemini Flash only for it to point out that my test was sending a request param called **query** when my API expected **pattern**. A good example of "I just need someone to look over my shoulder and tell me why this code that should work, does not" |
