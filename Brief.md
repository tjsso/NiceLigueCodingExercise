Statement:
We would like to create an API to manage the Nice football team in Ligue 1. The club's sporting director wants to store the list of players and the team's budget in a database to better manage the upcoming transfer market.

Requirements:

Create a REST API with two methods (adding other methods is not a requirement and will be considered a bonus):

• One that returns a list of teams, each containing a list of players.

The list will be paginated and can be sorted server-side (by team name, acronym, and budget).

• Another that allows adding a team with or without associated players (all other fields are required).

Upon project delivery, please send an installation guide to allow testing of the solution, as well as the source code. Each technical choice must be explained.

Example of a data model:
Team: [id, name, abbreviation, players, budget]
Player: [id, name, position]

The following will be evaluated:
- Architecture
- Selection techniques
- Comments/Javadoc/Logs
- Unit and integration tests.
